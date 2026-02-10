package org.example.digitalAdapter.custom;

import com.google.gson.JsonObject;
import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import org.example.digitalAdapter.configuration.DigitalRegisterConfiguration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;

public class RegisterDigitalAdapter extends DigitalAdapter<DigitalRegisterConfiguration> {

    public RegisterDigitalAdapter(String id, DigitalRegisterConfiguration configuration) {
        super(id, configuration);
    }

    public RegisterDigitalAdapter(String id) {
        super(id);
    }

    /**
     * Esegue una richiesta HTTP POST inviando un payload JSON.
     * * @param url L'URI completo del server di destinazione (es. http://localhost:8080/api/resource).
     * @param jsonPayload La stringa JSON da inviare nel corpo della richiesta.
     * @throws IOException Se si verifica un errore di I/O.
     * @throws InterruptedException Se il thread viene interrotto durante l'attesa della risposta.
     */
    public void sendPostRequest(String url, String jsonPayload) {
        try {
            System.out.println("Parte richiesta POST a: " + url);
            // 1. Crea un'istanza di HttpClient (riutilizzabile per più richieste)
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(10)) // Imposta un timeout di connessione
                    .build();

            // 2. Crea l'oggetto HttpRequest
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + "/registerDigitalTwin")) // Imposta l'URL di destinazione
                    .header("Content-Type", "application/json") // Indica al server che stiamo inviando JSON
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload)) // Imposta il metodo POST e il corpo della richiesta
                    .build();

            System.out.println("Invio richiesta POST a: " + url);
            System.out.println("Payload: " + jsonPayload);

            // 3. Invia la richiesta e ottieni la risposta
            // HttpResponse<String> specifica che la risposta (il corpo) deve essere gestita come String.

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 4. Gestisci e stampa lo stato della risposta
            System.out.println("\nRisposta ricevuta:");
            System.out.println("Status Code: " + response.statusCode());

            // Stampa il corpo della risposta del server
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Risposta (Successo): " + response.body());
            } else {
                System.err.println("Risposta (Errore): " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error in sending post request: " + e.getMessage());
        }
    }

    /**
     * Esegue una richiesta HTTP DELETE inviando un payload JSON.
     * * @param url L'URI completo del server di destinazione (es. http://localhost:8080/api/resource).
     * @throws IOException Se si verifica un errore di I/O.
     * @throws InterruptedException Se il thread viene interrotto durante l'attesa della risposta.
     */
    public void sendDeleteRequest(String url) throws IOException, InterruptedException {

        // 1. Crea un'istanza di HttpClient (riutilizzabile per più richieste)
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10)) // Imposta un timeout di connessione
                .build();

        // 2. Crea l'oggetto HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url)) // Imposta l'URL di destinazione
                .header("Content-Type", "application/json") // Indica al server che stiamo inviando JSON
                .DELETE() // Imposta il metodo POST e il corpo della richiesta
                .build();

        System.out.println("Invio richiesta DELETE a: " + url);

        // 3. Invia la richiesta e ottieni la risposta
        // HttpResponse<String> specifica che la risposta (il corpo) deve essere gestita come String.
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 4. Gestisci e stampa lo stato della risposta
        System.out.println("\nRisposta ricevuta:");
        System.out.println("Status Code: " + response.statusCode());

        // Stampa il corpo della risposta del server
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            System.out.println("Risposta (Successo): " + response.body());
        } else {
            System.err.println("Risposta (Errore): " + response.body());
        }
    }

    @Override
    protected void onStateUpdate(DigitalTwinState digitalTwinState, DigitalTwinState digitalTwinState1, ArrayList<DigitalTwinStateChange> arrayList) {

    }

    @Override
    protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {

    }

    @Override
    public void onAdapterStart() {
        JsonObject json = new JsonObject();
        json.addProperty("port", this.getConfiguration().getPort());
        json.addProperty("type", this.getConfiguration().getType());
        json.addProperty("name", this.getConfiguration().getName());
        System.out.println("INVIO QUESTO");
        System.out.println(json.toString());
        this.sendPostRequest(this.getConfiguration().getUrl(), json.toString());
        System.out.println("INVIO LA ROBA");
    }

    @Override
    public void onAdapterStop() {
        // TODO send stop message to dt-gateway
        try {
            this.sendDeleteRequest(this.getConfiguration().getUrl() + "/" + this.getConfiguration().getPort());
        } catch (IOException | InterruptedException e) {
            System.out.println("PROBLEMI PROBLEMI PROBLEMI");
        }
    }

    @Override
    public void onDigitalTwinSync(DigitalTwinState digitalTwinState) {

    }

    @Override
    public void onDigitalTwinUnSync(DigitalTwinState digitalTwinState) {

    }

    @Override
    public void onDigitalTwinCreate() {

    }

    @Override
    public void onDigitalTwinStart() {

    }

    @Override
    public void onDigitalTwinStop() {
        try {
            this.sendDeleteRequest(this.getConfiguration().getUrl() + "/" + this.getConfiguration().getPort());
        } catch (IOException | InterruptedException e) {
        }
    }

    @Override
    public void onDigitalTwinDestroy() {

    }
}
