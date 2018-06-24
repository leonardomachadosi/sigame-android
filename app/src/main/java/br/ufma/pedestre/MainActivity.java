package br.ufma.pedestre;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import br.ufma.lsdi.cddl.CDDL;
import br.ufma.lsdi.cddl.Callback;
import br.ufma.lsdi.cddl.Publisher;
import br.ufma.lsdi.cddl.Subscriber;
import br.ufma.lsdi.cddl.message.CommandRequest;
import br.ufma.lsdi.cddl.message.ContextMessage;
import br.ufma.lsdi.cddl.message.MOUUID;
import br.ufma.lsdi.cddl.message.SensorData;
import br.ufma.lsdi.cddl.message.TechnologyID;
import br.ufma.lsdi.cddl.type.CDDLConfig;
import br.ufma.lsdi.cddl.type.ClientId;
import br.ufma.lsdi.cddl.type.Host;
import br.ufma.lsdi.cddl.type.Topic;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    final private String TAG = MainActivity.class.getSimpleName();

    final Gson gson = new Gson();

    private CDDL cddl = CDDL.getInstance();

    private Publisher publisher;

    private Subscriber subscriber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        setPermissions();
        initCDDL();


    }


    private void initCDDL() {
        CDDLConfig config = CDDLConfig.builder()
                //.host(Host.of("tcp://lsdi.ufma.br:1883"))
                .host(Host.of("tcp://localhost:1883"))
                .clientId(ClientId.of("leonardo.machado@lsdi.ufma.br"))
                .build();

        cddl.init(this, config);
        cddl.startScan();
    }


    public void listarSensoresInternos() {
        List<String> sensors = cddl.getInternalSensorList();

        for (String sen : sensors) {
            Log.d("Sensors", sen);
        }
        //final ListAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, sensors);
        // listView.setAdapter(adapter);
    }

    @OnClick(R.id.button)
    public void publicarMeuSensorLocation() {
        List<String> sensorList = Arrays.asList("Goldfish 3-axis Accelerometer");
        CommandRequest comandRequest = new CommandRequest("leonardo.machado@lsdi.ufma.br", new MOUUID(TechnologyID.INTERNAL.id, "localhost"), "start-sensors", sensorList);

        Log.d("Location", comandRequest.toString());

        publisher = Publisher.of(cddl);
        publisher.setCallback(new Callback() {
            @Override
            public void onConnectSuccess() {
                publisher.publish(comandRequest);
            }

            @Override
            public void onConnectFailure(Throwable exception) {
                Toast.makeText(getApplicationContext(), "Deu algum erro", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPublishSuccess(Topic topic) {
                Toast.makeText(getApplicationContext(), "Publicou direitinho", Toast.LENGTH_SHORT).show();
            }
        });
        publisher.connect();

    }

    @OnClick(R.id.sub)
    public void subescreverLocation() {

        subscriber = Subscriber.of(cddl);
        subscriber.setCallback(new Callback() {
            @Override
            public void messageArrived(ContextMessage contextMessage) {
                Gson gson = new Gson();
                String type = contextMessage.getType();
                if (type.equals(SensorData.class.getSimpleName())) {
                    SensorData sensorData = gson.fromJson(contextMessage.getBody(), SensorData.class);
                    Log.d("UpdateLocation", sensorData.toString());

                }
            }


            @Override
            public void onConnectSuccess() {
                subscriber.subscribe(Topic.of("leonardo.machado@lsdi.ufma.br/Goldfish 3-axis Accelerometer"));
                Toast.makeText(getApplicationContext(), "vamos sobescrever", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectFailure(Throwable exception) {
                Toast.makeText(getApplicationContext(), "opsss", Toast.LENGTH_SHORT).show();
            }
        });
        subscriber.connect();

    }

    public void publicar() {
        ContextMessage contextMessage = new ContextMessage("String", "User Action", "Walking");


        publisher = Publisher.of(cddl);
        publisher.setCallback(new Callback() {
            @Override
            public void onConnectSuccess() {
                publisher.publish(contextMessage);
            }

            @Override
            public void onConnectFailure(Throwable exception) {
                Toast.makeText(getApplicationContext(), "Deu algum erro", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onPublishSuccess(Topic topic) {

                Toast.makeText(getApplicationContext(), "Publicou direitinho", Toast.LENGTH_SHORT).show();
            }
        });
        publisher.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cddl.stopScan();
    }

    private void setPermissions() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

    }

}
