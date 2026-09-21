package config;

import com.astier.bts.client_tcp.modele.Config_AES;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class Lecture_Json {

    public String path;

    public Lecture_Json(String ficherPath){
        this.path = ficherPath;
    }

    public Config_AES getConfAES() throws FileNotFoundException {
        Gson gson = new Gson();
        FileReader fileReader = new FileReader(path);
        JsonReader jsonReader = new JsonReader(fileReader);
        return gson.fromJson(jsonReader,Config_AES.class);
    }
}
