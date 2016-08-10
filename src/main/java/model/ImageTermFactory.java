package model;

// [START import_libraries]

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.common.collect.ImmutableList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
// [END import_libraries]

/**
 * A sample application that uses the Vision API to label an image.
 */
@SuppressWarnings("serial")
public class ImageTermFactory {

    private static final String APPLICATION_NAME = "codeu-final-project/1.0";

    private static final int MAX_LABELS = 300;

    // [START run_application]

    /**
     * Annotates an image using the Vision API.
     */
    public static void main(String[] args) throws IOException, GeneralSecurityException {
    if (args.length != 1) {
      System.err.println("Missing imagePath argument.");
      System.err.println("Usage:");
      System.err.printf("\tjava %s imagePath\n", ImageTermFactory.class.getCanonicalName());
      System.exit(1);
    }
//    Path imagePath = Paths.get(args[0]);
//
    ImageTermFactory app = new ImageTermFactory(getVisionService());
    printLabels(System.out, args[0], app.labelImage(new URL(args[0]), MAX_LABELS));
    }

    public static Map<String ,Map<String, Double>> getTermMap(List<String> image_urls) throws IOException, GeneralSecurityException {
        ImageTermFactory app = new ImageTermFactory(getVisionService());
        Map<String, Map<String, Double>> index = new HashMap<>();

        for (String image_url : image_urls) {
            URL url = new URL(image_url);
            List<EntityAnnotation> labels = app.labelImage(url, MAX_LABELS);
            if (labels != null) {
                Map<String, Double> terms = new HashMap<>();
                for (EntityAnnotation label : labels) {
                    terms.put(label.getDescription(), label.getScore().doubleValue());
                }
                index.put(image_url, terms);
            }
        }

        return index;
    }

    public static Map<String, Double> getRelevantURLs(Map<String ,Map<String, Double>> index, String term) {
        Map<String, Double> links = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> ele : index.entrySet()) {
            if (ele.getValue().containsKey(term)) {
                links.put(ele.getKey(), ele.getValue().get(term));
            }
        }
        return links;
    }


    /**
     * Prints the labels received from the Vision API.
     */
    public static void printLabels(PrintStream out, String imagePath, List<EntityAnnotation> labels) {
        out.printf("Labels for image %s:\n", imagePath);
        for (EntityAnnotation label : labels) {
            out.printf(
                    "\t%s (score: %.3f)\n",
                    label.getDescription(),
                    label.getScore());
        }
        if (labels.isEmpty()) {
            out.println("\tNo labels found.");
        }
    }
    // [END run_application]

    // [START authenticate]

    /**
     * Connects to the Vision API using Application Default Credentials.
     */
    public static Vision getVisionService() throws IOException, GeneralSecurityException {
        GoogleCredential credential =
                GoogleCredential.getApplicationDefault().createScoped(VisionScopes.all());
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        return new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    // [END authenticate]

    private final Vision vision;

    /**
     * Constructs a {@link ImageTermFactory} which connects to the Vision API.
     */
    public ImageTermFactory(Vision vision) {
        this.vision = vision;
    }

    /**
     * Gets up to {@code maxResults} labels for an image stored at {@code path}.
     */
    public List<EntityAnnotation> labelImage(URL url, int maxResults) throws IOException {
        // [START construct_request]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream inputStream = null;
        try {
            inputStream = url.openStream();
            byte[] byteChunk = new byte[4096];
            int n;

            while ((n = inputStream.read(byteChunk)) > 0) {
                baos.write(byteChunk, 0, n);
            }
        } catch (IOException e) {
            System.err.printf("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
            e.printStackTrace();
            // Perform any other exception handling that's appropriate.
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        byte[] data = baos.toByteArray();

        AnnotateImageRequest request =
                new AnnotateImageRequest()
                        .setImage(new Image().encodeContent(data))
                        .setFeatures(ImmutableList.of(
                                new Feature()
                                        .setType("LABEL_DETECTION")
                                        .setMaxResults(maxResults)));
        Vision.Images.Annotate annotate =
                vision.images()
                        .annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        // annotate.setDisableGZipContent(true);
        // [END construct_request]

        // [START parse_response]
        BatchAnnotateImagesResponse batchResponse = annotate.execute();
        assert batchResponse.getResponses().size() == 1;
        AnnotateImageResponse response = batchResponse.getResponses().get(0);
        if (response.getLabelAnnotations() == null) {
            return null;
//            throw new IOException(
//                    response.getError() != null
//                            ? response.getError().getMessage()
//                            : "Unknown error getting image annotations");
        }
        return response.getLabelAnnotations();
        // [END parse_response]
    }
}
