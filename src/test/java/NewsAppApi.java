import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewsAppApi {
    public static void main(String[] args) throws Exception {
        String apiKey = "";
        String url = "https://newsapi.org/v2/top-headlines?country=br&apiKey=" + apiKey;
        HttpGet httpGet = new HttpGet(url);
        HttpClient httpclient = HttpClients.createDefault();

        try {
            HttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);

            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray articles = jsonObject.getJSONArray("articles");

            for (int i = 0; i < articles.length(); i++) {
                JSONObject article = articles.getJSONObject(i);
                String title = article.getString("title");
                String site = article.getJSONObject("source").getString("name");
                String dateString = article.getString("publishedAt");
                String dateFormatted = formatDate(dateString);
                String description = article.optString("description", ""); // get description or empty string if not present
                String content = article.optString("content", ""); // get content or empty string if not present
                String text = truncateText(content, 53);

                System.out.println(title + " " + dateFormatted + " " + site);
                System.out.println(text);
                System.out.println();

                // save image to file
                String urlToImage = article.optString("urlToImage", ""); // get image URL or empty string if not present
                if (!urlToImage.isEmpty()) {
                    byte[] imageBytes = downloadImage(urlToImage);
                    saveImageToFile(imageBytes, title);
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] downloadImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        BufferedImage image = ImageIO.read(url);
        File tempFile = Files.createTempFile("temp-", ".jpg").toFile();
        ImageIO.write(image, "jpg", tempFile);
        return Files.readAllBytes(tempFile.toPath());
    }

    private static void saveImageToFile(byte[] imageBytes, String title) throws IOException {
        String fileName = title.replaceAll("[^a-zA-Z0-9.-]", "_") + ".jpg";
        String filePath = "C:\\Users\\migue\\OneDrive\\Documentos\\IFG\\JAVA\\Porjetos java\\NewsApi\\IMGNot\\" + fileName; // adicionar a barra invertida ao final
        File file = new File(filePath);
        Files.write(file.toPath(), imageBytes);
    }

    private static String formatDate(String dateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try {
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String truncateText(String text, int length) {
        if (text.length() > length) {
            text = text.substring(0, length - 3) + "...";
        }
        return text;
    }
}
