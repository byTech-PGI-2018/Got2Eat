package bytech.got2eat;

import android.os.AsyncTask;

import okhttp3.*;
import com.google.gson.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;


public class HerePlaces{

    String returnValue = "";
    /*OkHttpClient client = new OkHttpClient();

    String doGetRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }*/

    class GetRequest extends AsyncTask<String,String,String> {
        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(String... strings) {
            Request request = new Request.Builder()
                    .url(strings[0])
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                returnValue = response.body().string();
                return returnValue;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

    }


    public static final MediaType JSON
            = MediaType.parse("application/json");


    public ArrayList parse(String jsonLine) throws IOException, ExecutionException, InterruptedException {
        ArrayList<Restaurant> restaurantArrayList = new ArrayList<>();
        JsonElement jelement = new JsonParser().parse(jsonLine);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("results");
        JsonArray jarray = jobject.getAsJsonArray("items");
        for (int i=0;i<jarray.size();i++){
            jobject = jarray.get(i).getAsJsonObject();
            String name = jobject.get("title").getAsString();
            String address = jobject.get("vicinity").getAsString().replace("<br/>"," ");;
            String distance = jobject.get("distance").getAsString();
            String link = jobject.get("href").getAsString();
            new GetRequest().execute(link).get();
            String moreInfoResponse = returnValue;

            /*======================== Mais Info Como: Contatos e Link do Mapa =========================*/
            String phoneNumber = "";
            JsonElement newElement = new JsonParser().parse(moreInfoResponse);
            JsonObject moreInfoObject = newElement.getAsJsonObject();
            String map = moreInfoObject.get("view").getAsString();
            moreInfoObject = moreInfoObject.getAsJsonObject("contacts");
            JsonArray phoneArray = moreInfoObject.getAsJsonArray("phone");
            try{
                if(phoneArray.size() > 0){
                    JsonObject phone = phoneArray.get(0).getAsJsonObject();
                    phoneNumber = phone.get("value").getAsString();
                }
            }catch (NullPointerException n){
                phoneNumber = "Sem informações";
            }
            /*===========================================================================================*/

            Restaurant restaurant = new Restaurant(name,address,phoneNumber,distance,map);
            restaurantArrayList.add(restaurant);
        }

        Collections.sort(restaurantArrayList);


        return restaurantArrayList;
    }

    public ArrayList returnRestaurant(double latitude, double longitude) throws IOException, ExecutionException, InterruptedException {

        HerePlaces example = new HerePlaces();

        new GetRequest().execute("https://places.api.here.com/places/v1/discover/explore?" +
                "in=" + latitude + "%2C" + longitude +                                  //Coords
                "%3Br%3D1000" +                                                         //Raio de Procura
                "&cat=restaurant" +                                                     //Categoria hotel%2C+
                "&size=20" +                                                            //Numero de resultados
                "&Accept-Language=pt-PT%2Cpt%3Bq%3D0.9%2Cen-US%3Bq%3D0.8%2Cen%3Bq%3D0.7" +
                "&app_id=JzDuBoAKw0kriebzj4nO" +
                "&app_code=JVnKvKtMrA1b08eQ8XJ4sw").get();
        //System.out.println(returnValue);
        String places1 = returnValue;

        //COIMBRA -> in=40.209202%2C-8.41936
        //TABUACO -> in=41.11708%2C-7.56277
        //VALE DE FIGUEIRA -> in=41.09152%2C-7.59195
        ArrayList res = example.parse(places1);

        //System.out.println(res.toString());

        //return res.toString();
        return res;
    }
}

class Restaurant implements Comparable{

    String nome;
    String morada;
    String telefone;
    String distancia;
    String link;

    public Restaurant(String nome, String morada, String telefone, String distancia, String link){
        this.morada = morada;
        this.nome = nome;
        this.telefone = telefone;
        this.distancia = distancia;
        this.link = link;
    }

    public String toString(){
        return "[" + nome + "] em " + morada + " a cerca de " + distancia + " metros.\n"; //+ "Ligue para " + telefone + " ou siga as indicações GPS em " + link + "\n";
    }


    @Override
    public int compareTo(Object o) {
        Restaurant r = (Restaurant) o;
        int compareDist = Integer.parseInt(r.distancia);
        return Integer.parseInt(this.distancia) - compareDist;
    }
}

