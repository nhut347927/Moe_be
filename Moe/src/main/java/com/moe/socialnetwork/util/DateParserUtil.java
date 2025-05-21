package com.moe.socialnetwork.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class DateParserUtil {
    private static final Map<String, DateTimeFormatter> formatters = new HashMap<>();

    static {
        formatters.put("dd-MM-yyyy", DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        formatters.put("yyyy/MM/dd", DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        formatters.put("MMMM dd, yyyy", DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
        formatters.put("EEE, MMM d, yyyy", DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"));
        formatters.put("MM/dd/yyyy", DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        formatters.put("yyyy.MM.dd", DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }
   /*
     * DateParser dateParser = new DateParser();
     * 
     * String inputDate = "26-10-2024";
     * String inputFormat = "dd-MM-yyyy";
     * LocalDateTime parsedDate = dateParser.parseDate(inputDate, inputFormat);
     *
     * if (parsedDate != null) {
     *    String outputFormat = "yyyy/MM/dd";
     *    String formattedDate = dateParser.formatDate(parsedDate, outputFormat);
     *    System.out.println("Ngày đã định dạng lại: " + formattedDate);
     * }
     * 
     * */
  
    /**
	 * Đầu vào	            Định dạng mong muốn	          Kết quả
	 *
	 * "26-10-2024"	        "dd-MM-yyyy"				  "26-10-2024"
	 * "October 26, 2024"	"MMMM dd, yyyy"	              "October 26, 2024"
	 * "10/26/2024"	        "MM/dd/yyyy"	              "10/26/2024"
	 * "2024.10.26"	        "EEE, MMM d, yyyy"	          "Sat, Oct 26, 2024"
	 * "2024/10/26"			"yyyy/MM/dd"	              "2024/10/26"
     * 
     * 
     * Chuyển chuỗi ngày thành đối tượng LocalDateTime theo định dạng mong muốn.
     *
     * @param dateString Chuỗi đầu vào.
     * @param format     Định dạng của chuỗi đầu vào.
     * @return LocalDateTime tương ứng hoặc null nếu định dạng không hợp lệ.
     */
    public LocalDateTime parseDate(String dateString, String format) {
        DateTimeFormatter formatter = formatters.get(format);
        if (formatter != null) {
            try {
                return LocalDateTime.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Không thể phân tích ngày với định dạng: " + format);
            }
        }
        return null;
    }

    /**
     * Định dạng đối tượng LocalDateTime thành chuỗi theo định dạng mong muốn.
     *
     * @param date        Đối tượng LocalDateTime cần định dạng.
     * @param outputFormat Định dạng mong muốn cho chuỗi kết quả.
     * @return Chuỗi biểu diễn ngày giờ theo định dạng.
     */
    public String formatDate(LocalDateTime date, String outputFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(outputFormat);
        return date.format(formatter);
    }

}
