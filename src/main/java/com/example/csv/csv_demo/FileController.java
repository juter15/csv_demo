package com.example.csv.csv_demo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.*;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class FileController {
    private static String GEOCODE_URL= "https://dapi.kakao.com/v2/local/search/address.json?query=";
    private static String APIKey="11ed3fb2254f086103bd3009990e7c15";

    @GetMapping("/")
    public String fileIndex(){
        return "fileupload";
    }
    @GetMapping("/test")
    public String test(String local){

        //Float[] lalo = findGeoPoint(local);
        return "fileupload";
    }
    @PostMapping("/upload-csv-file")
    public String fileUpdate(@RequestParam("file") MultipartFile file, Model model){
        //commons-io dependency BOMInputStream 첫열 못읽어오는 오류 해결
        try (Reader reader = new BufferedReader(new InputStreamReader(new BOMInputStream(file.getInputStream())))) {


            List<CsvModel> models = new CsvToBeanBuilder<CsvModel>(reader)
                    .withType(CsvModel.class)
                    .build()
                    .parse();
            // TODO: save users in DB?
            for(CsvModel csvModel : models){
                //log.info("{}",addrToCoord(csvModel.getAddress()));
                Float[] getCoodr = addrToCoord(csvModel.getAddress());
                log.info("x:{}, y:{}", getCoodr[0], getCoodr[1]);
                csvModel.setLatitude(getCoodr[0]);
                csvModel.setLongitude(getCoodr[1]);
                //log.info("name: {}, 위도: {}, 경도: {}", csvModel.getName(), lalo[0], lalo[1]);
                log.info("models: {}", models);

            }

        } catch (Exception ex) {
            model.addAttribute("message", "An error occurred while processing the CSV file.");

        }
        return "fileupload";
    }

    public Float[] addrToCoord(String addr) throws Exception {
        String addressEncode = URLEncoder.encode(addr, "UTF-8");
        Float[] coords = new Float[2];
        try{
            String jsonString = "";
            String buf;

            URL url = new URL(GEOCODE_URL+addressEncode);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            String auth = "KakaoAK "+APIKey;
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Requested-With", "curl");
            conn.setRequestProperty("Authorization", auth);


            BufferedReader br = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), "UTF-8"));
            while ((buf = br.readLine()) != null) {
                jsonString += buf;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            //objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            KakaoGeoRes bodyJson = objectMapper.readValue(jsonString, KakaoGeoRes.class);
            log.info("bodyJson: {}", bodyJson);
            coords[0] = bodyJson.getDocuments().get(0).getX().floatValue();
            coords[1] = bodyJson.getDocuments().get(0).getY().floatValue();
            //log.info("zone_no: {}", bodyJson.getDocuments().get(0).getRoad_address().get());
            //log.info("zone: {}", bodyJson.getDocuments().get(0).getRoad_address().get("zone_no"));
            if(bodyJson.getDocuments().get(0).getRoad_address() == null){
                log.info("zone: null");
            }
            else{
                log.info("zone: {}", bodyJson.getDocuments().get(0).getRoad_address().get("zone_no"));
            }

        }catch(Exception e){

            e.printStackTrace();
        }
        return coords;

    }



}
