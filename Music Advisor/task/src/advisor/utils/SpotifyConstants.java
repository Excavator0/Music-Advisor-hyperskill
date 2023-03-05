package advisor.utils;

public class SpotifyConstants {

    public static final String ACCESS_CODE_URL = "https://accounts.spotify.com/authorize?client_id=%s&response_type=code&redirect_uri=%s";
    public static final String SPOTIFY_SERVER_PATH = "https://accounts.spotify.com";
    public static final String SPOTIFY_API_SERVER_PATH = "https://api.spotify.com";
    public static final String REDIRECT_URI = "http://localhost:8080";
    public static final String REQUEST_BODY_PARAMETER = "grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s";
    public static final String CLIENT_ID = "24f6edba6cd8463a80042d4a1532477e";
    public static final String CLIENT_SECRET = "84cfb5fe5da043178602c4db0704da23";

    public static String CODE;
    public static String TOKEN;
}