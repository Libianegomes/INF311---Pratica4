package br.ufv.inf311.pratica04leituras;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {

    public static final String ACTION_CLASSIFICAR_LEITURAS = "br.ufv.inf311.intent.ACTION_CLASSIFICAR_LEITURAS";
    public static final String EXTRA_LUMINOSIDADE = "br.ufv.inf311.extra.LUMINOSIDADE";
    public static final String EXTRA_PROXIMIDADE = "br.ufv.inf311.extra.PROXIMIDADE";
    public static final String EXTRA_LIGAR_LANTERNA = "br.ufv.inf311.extra.LIGAR_LANTERNA";
    public static final String EXTRA_LIGAR_VIBRACAO = "br.ufv.inf311.extra.LIGAR_VIBRACAO";

    private static final int REQUEST_CODE_CLASSIFICACAO = 4004;
    private static final int REQUEST_CODE_CAMERA = 2304;

    private Switch switchLanterna;
    private Switch switchVibracao;

    private SensorManager sensorManager;
    private Sensor sensorLuminosidade;
    private Sensor sensorProximidade;

    private float ultimaLuminosidade = Float.NaN;
    private float ultimaProximidade = Float.NaN;

    private LanternaHelper lanternaHelper;
    private MotorHelper motorHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchLanterna = findViewById(R.id.switchLanterna);
        switchVibracao = findViewById(R.id.switchVibracao);
        Button buttonClassificar = findViewById(R.id.buttonClassificar);

        prepararSwitchNaoClicavel(switchLanterna);
        prepararSwitchNaoClicavel(switchVibracao);

        lanternaHelper = new LanternaHelper(this);
        motorHelper = new MotorHelper(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            sensorLuminosidade = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            sensorProximidade = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        solicitarPermissaoCameraQuandoNecessario();

        buttonClassificar.setOnClickListener(view -> classificarLeituras());
    }

    private void prepararSwitchNaoClicavel(Switch componente) {
        componente.setClickable(false);
        componente.setFocusable(false);
        componente.setEnabled(false);
    }

    private void solicitarPermissaoCameraQuandoNecessario() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registrarSensores();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    private void registrarSensores() {
        if (sensorManager == null) {
            return;
        }

        if (sensorLuminosidade != null) {
            sensorManager.registerListener(this, sensorLuminosidade, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (sensorProximidade != null) {
            sensorManager.registerListener(this, sensorProximidade, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void classificarLeituras() {
        if (Float.isNaN(ultimaLuminosidade) || Float.isNaN(ultimaProximidade)) {
            Toast.makeText(this, R.string.erro_sensores, Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(ACTION_CLASSIFICAR_LEITURAS);
        intent.putExtra(EXTRA_LUMINOSIDADE, ultimaLuminosidade);
        intent.putExtra(EXTRA_PROXIMIDADE, ultimaProximidade);

        try {
            startActivityForResult(intent, REQUEST_CODE_CLASSIFICACAO);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.erro_app_classificador, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_CODE_CLASSIFICACAO || resultCode != RESULT_OK || data == null) {
            return;
        }

        boolean deveLigarLanterna = data.getBooleanExtra(EXTRA_LIGAR_LANTERNA, false);
        boolean deveLigarVibracao = data.getBooleanExtra(EXTRA_LIGAR_VIBRACAO, false);

        atualizarLanterna(deveLigarLanterna);
        atualizarVibracao(deveLigarVibracao);
    }

    private void atualizarLanterna(boolean ligar) {
        if (ligar) {
            lanternaHelper.ligar();
        } else {
            lanternaHelper.desligar();
        }
        switchLanterna.setChecked(ligar);
    }

    private void atualizarVibracao(boolean ligar) {
        if (ligar) {
            motorHelper.iniciarVibracao();
        } else {
            motorHelper.pararVibracao();
        }
        switchVibracao.setChecked(ligar);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null || event.sensor == null || event.values.length == 0) {
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            ultimaLuminosidade = event.values[0];
        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            ultimaProximidade = event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Não há tratamento específico de acurácia exigido para esta atividade.
    }

    @Override
    protected void onDestroy() {
        desligarRecursosFisicos();
        super.onDestroy();
    }

    private void desligarRecursosFisicos() {
        if (lanternaHelper != null) {
            lanternaHelper.desligar();
        }
        if (motorHelper != null) {
            motorHelper.pararVibracao();
        }
    }
}
