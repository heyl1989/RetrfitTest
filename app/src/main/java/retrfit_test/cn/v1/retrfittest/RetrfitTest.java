package retrfit_test.cn.v1.retrfittest;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;


/**
 * Created by qy on 2018/1/29.
 */

public class RetrfitTest {

    private static final long TIMEOUT = 30;

    public static void main(String[] args) {
        try {
            retrfitTest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void retrfitTest() throws IOException {
        //创建一个非常简单的指向GitHub API的REST适配器。
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //创建我们的GitHub API接口的一个实例。
        GitHub github = retrofit.create(GitHub.class);
        //创建一个调用实例来查找Retrofit贡献者。
        Call<List<Contributor>> call = github.contributors("square", "retrofit");
        //获取并打印库的贡献者列表。
        Response response = call.execute();
        System.out.println(response.code() + "\n"
                + response.toString() + "\n"
                + new Gson().toJson(response.body()));
    }

    public static class Contributor {
        public final String login;
        public final int contributions;

        public Contributor(String login, int contributions) {
            this.login = login;
            this.contributions = contributions;
        }
    }

    //https://api.github.com/repos/square/retrofit/contributors
    public static final String API_URL = "https://api.github.com";
    public interface GitHub {
        @GET("/repos/{owner}/{repo}/contributors")
        Call<List<Contributor>> contributors(
                @Path("owner") String owner,
                @Path("repo") String repo);
    }

    private static OkHttpClient httpClient = new OkHttpClient.Builder()
            .addInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    System.out.println(message);
                }
            }).setLevel(HttpLoggingInterceptor.Level.BASIC))
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build();

}
