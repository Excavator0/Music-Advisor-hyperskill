package advisor;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.Scanner;

import static advisor.app.Advisor.init;
import static advisor.utils.SpotifyConstants.SPOTIFY_API_SERVER_PATH;
import static advisor.utils.SpotifyConstants.SPOTIFY_SERVER_PATH;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        String serverPath = SPOTIFY_SERVER_PATH;
        String apiPath = SPOTIFY_API_SERVER_PATH;
        int contentPerPage = 5;

        if (args.length > 1) {
            if ("-access".equals(args[0])) {
                serverPath = args[1];
            }

            if ("-resource".equals(args[2])) {
                apiPath = args[3];
            }

            if ("-page".equals(args[4])) {
                contentPerPage = Integer.parseInt(args[5]);
            }
        }

        init(new Scanner(System.in), HttpClient.newBuilder().build(), serverPath, apiPath, contentPerPage);
    }

}
