package com.internship.service;

import com.internship.dto.Dtos.StatsDto;
import com.internship.model.Application;
import com.internship.model.DailyLog;
import com.internship.model.InternshipListing;
import com.internship.model.User;
import com.internship.repository.*;
import com.internship.util.SimplePdf;
import com.internship.web.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final UserRepository users;
    private final InternshipListingRepository listings;
    private final ApplicationRepository apps;
    private final DailyLogRepository logs;

    public record Export(byte[] data, String contentType, String filename) {}

    public StatsDto stats() {
        long accepted = apps.countByStatus(Application.Status.ACCEPTED);
        long completed = apps.countByCompletedTrue();
        double rate = accepted == 0 ? 0 : Math.round(completed * 1000.0 / accepted) / 10.0;
        return new StatsDto(users.countByRole(User.Role.STUDENT), users.countByRole(User.Role.COMPANY),
                users.countByRoleAndStatus(User.Role.COMPANY, User.Status.PENDING),
                listings.countByStatus(InternshipListing.Status.OPEN), apps.count(), accepted, completed, rate);
    }

    public byte[] certificate(Application a) {
        SimplePdf p = new SimplePdf(842, 595);
        p.rect(24, 24, 794, 547, 3);
        p.rect(34, 34, 774, 527, 1);
        p.centered(440, 34, true, "Certificate of Completion");
        p.centered(395, 14, false, "This is to certify that");
        p.centered(345, 30, true, a.getIntern().getName());
        p.centered(305, 14, false, "has successfully completed the internship");
        p.centered(262, 22, true, a.getListing().getTitle());
        p.centered(228, 16, false, "at " + a.getListing().getCompany().getName());
        if (a.getRating() != null) p.centered(190, 13, false, "Performance rating: " + a.getRating() + " out of 5");
        p.centered(165, 13, false, "Completed on " + a.getCompletedOn());
        p.centered(75, 10, false, "Certificate no. SIM-" + String.format("%05d", a.getId()));
        return p.toBytes();
    }

    public Export export(String type, String format) {
        List<String> head;
        double[] weights;
        String title;
        List<List<String>> rows = new ArrayList<>();
        if ("applications".equals(type)) {
            title = "Applications report";
            head = List.of("ID", "Intern", "Email", "Internship", "Company", "Status", "Applied", "Rating", "Done");
            weights = new double[]{0.5, 1.4, 2.2, 2.2, 1.6, 1.3, 1.2, 0.7, 0.7};
            for (Application a : apps.findAll(Sort.by("id")))
                rows.add(List.of(s(a.getId()), s(a.getIntern().getName()), s(a.getIntern().getEmail()),
                        s(a.getListing().getTitle()), s(a.getListing().getCompany().getName()), s(a.getStatus()),
                        s(a.getAppliedDate()), s(a.getRating()), a.isCompleted() ? "Yes" : "No"));
        } else if ("logs".equals(type)) {
            title = "Daily logs report";
            head = List.of("ID", "Intern", "Internship", "Date", "Tasks", "Status", "Mentor notes");
            weights = new double[]{0.5, 1.3, 1.7, 1.1, 3.2, 1.0, 2.2};
            for (DailyLog d : logs.findAll(Sort.by("id")))
                rows.add(List.of(s(d.getId()), s(d.getApplication().getIntern().getName()),
                        s(d.getApplication().getListing().getTitle()), s(d.getDate()), s(d.getTasksCompleted()),
                        s(d.getStatus()), s(d.getMentorNotes())));
        } else {
            throw ApiException.bad("Report type must be applications or logs.");
        }
        boolean pdf = "pdf".equalsIgnoreCase(format);
        if (!pdf && !"csv".equalsIgnoreCase(format)) throw ApiException.bad("Format must be csv or pdf.");
        String name = type + "-report." + (pdf ? "pdf" : "csv");
        return pdf ? new Export(pdf(title, head, rows, weights), "application/pdf", name)
                   : new Export(csv(head, rows), "text/csv", name);
    }

    private byte[] csv(List<String> head, List<List<String>> rows) {
        StringBuilder b = new StringBuilder("\uFEFF");
        b.append(String.join(",", head.stream().map(ReportService::cell).toList())).append("\r\n");
        for (List<String> r : rows) b.append(String.join(",", r.stream().map(ReportService::cell).toList())).append("\r\n");
        return b.toString().getBytes(StandardCharsets.UTF_8);
    }

    /** Quotes fields and neutralises spreadsheet formulas (CSV injection). */
    private static String cell(String v) {
        String x = v == null ? "" : v;
        if (!x.isEmpty() && "=+-@".indexOf(x.charAt(0)) >= 0) x = "'" + x;
        if (x.contains(",") || x.contains("\"") || x.contains("\n") || x.contains("\r"))
            x = "\"" + x.replace("\"", "\"\"") + "\"";
        return x;
    }

    private byte[] pdf(String title, List<String> head, List<List<String>> rows, double[] weights) {
        SimplePdf p = new SimplePdf(595, 842);
        double total = Arrays.stream(weights).sum();
        double[] xs = new double[weights.length];
        int[] max = new int[weights.length];
        double x = 40;
        for (int i = 0; i < weights.length; i++) {
            double w = 515 * weights[i] / total;
            xs[i] = x;
            max[i] = Math.max(3, (int) (w / 4.4) - 1);
            x += w;
        }
        double y = 800;
        p.text(40, y, 16, true, title);
        y -= 16;
        p.text(40, y, 9, false, "Generated " + LocalDate.now() + ", " + rows.size() + " records");
        y -= 28;
        row(p, xs, max, head, y, true);
        y -= 16;
        for (List<String> r : rows) {
            if (y < 50) {
                p.newPage();
                y = 800;
                row(p, xs, max, head, y, true);
                y -= 16;
            }
            row(p, xs, max, r, y, false);
            y -= 14;
        }
        return p.toBytes();
    }

    private static void row(SimplePdf p, double[] xs, int[] max, List<String> cells, double y, boolean bold) {
        for (int i = 0; i < xs.length; i++) {
            String t = cells.get(i).replaceAll("\\s+", " ");
            if (t.length() > max[i]) t = t.substring(0, max[i] - 2) + "..";
            p.text(xs[i], y, 8, bold, t);
        }
    }

    private static String s(Object o) { return o == null ? "" : String.valueOf(o); }
}
