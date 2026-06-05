package br.ufv.inf311.pratica04classificador;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends Activity {

    public static final String EXTRA_LUMINOSIDADE = "br.ufv.inf311.extra.LUMINOSIDADE";
    public static final String EXTRA_PROXIMIDADE = "br.ufv.inf311.extra.PROXIMIDADE";
    public static final String EXTRA_LIGAR_LANTERNA = "br.ufv.inf311.extra.LIGAR_LANTERNA";
    public static final String EXTRA_LIGAR_VIBRACAO = "br.ufv.inf311.extra.LIGAR_VIBRACAO";

    private static final float LIMITE_LUMINOSIDADE_BAIXA = 20.0f;
    private static final float LIMITE_PROXIMIDADE_DISTANTE = 3.0f;

    private float luminosidadeRecebida;
    private float proximidadeRecebida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recuperarLeiturasRecebidas();

        Button buttonDevolverClassificacoes = findViewById(R.id.buttonDevolverClassificacoes);
        buttonDevolverClassificacoes.setOnClickListener(view -> devolverClassificacoes());
    }

    private void recuperarLeiturasRecebidas() {
        Intent intentRecebida = getIntent();
        luminosidadeRecebida = intentRecebida.getFloatExtra(EXTRA_LUMINOSIDADE, 0.0f);
        proximidadeRecebida = intentRecebida.getFloatExtra(EXTRA_PROXIMIDADE, 0.0f);
    }

    private void devolverClassificacoes() {
        boolean ligarLanterna = luminosidadeRecebida < LIMITE_LUMINOSIDADE_BAIXA;
        boolean ligarVibracao = proximidadeRecebida > LIMITE_PROXIMIDADE_DISTANTE;

        Intent resultado = new Intent();
        resultado.putExtra(EXTRA_LIGAR_LANTERNA, ligarLanterna);
        resultado.putExtra(EXTRA_LIGAR_VIBRACAO, ligarVibracao);

        setResult(RESULT_OK, resultado);
        finish();
    }
}
