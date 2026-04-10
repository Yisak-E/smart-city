package smart_city;

import java.io.*;
import java.nio.charset.StandardCharsets; 

public final class MessageUtill{
	private MessageUtill() {
		
	}
	
	public static String readAll(InputStream input) throws IOException{
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(input, StandardCharsets.UTF_8)
				);
		
		StringBuilder sb = new StringBuilder();
		String line ;
		
		boolean first = true;
		
		while ((line = reader.readLine()) != null) {
			if(!first) {
				sb.append('\n');
			}
			sb.append(line);
			first = false;
		}
		return sb.toString();
				
	}
	
	public static void writeText(OutputStream output, String text) throws IOException {
		PrintWriter writer = new PrintWriter(output, false,StandardCharsets.UTF_8 );
		writer.println(text);
		writer.flush();
		writer.close();
	}
}