package com.sanabria.ping;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;

public class FirstFragment extends Fragment {

    private static final String NUMERO_DE_PAQUETES_POR_DEFECTO = "4";
    private FFMutableLiveData ffMutableLiveData;
    private TextInputLayout tilUrl, tilNumeroPaquetes;
    private TextView txvBuffer;
    private Button btnEnviarPing;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        observadores();
        listeners();

    }

    private void observadores() {

        ffMutableLiveData = new ViewModelProvider(requireActivity()).get(FFMutableLiveData.class);

        ffMutableLiveData.getInfo()
                .observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String lineaEnBuffer) {
                        txvBuffer.setText(lineaEnBuffer);
                    }
                });

        ffMutableLiveData.isPingRunning()
                .observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean isPingRunning) {

                        if (!isPingRunning) {
                            btnEnviarPing.setText(getResources().getString(R.string.enviar_ping));
                        }
                        else {
                            btnEnviarPing.setText(getResources().getString(R.string.detener_ping));
                        }
                    }
                });

    }

    private void initView(View view) {
        tilUrl = view.findViewById(R.id.tilUrl);
        tilNumeroPaquetes = view.findViewById(R.id.tilNumPaquetes);
        txvBuffer = view.findViewById(R.id.txvBuffer);
        btnEnviarPing = view.findViewById(R.id.btnEnviarPing);
    }

    private void listeners() {
        btnEnviarPing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ffMutableLiveData.executorisShutDown()){

                    nuevoPing();
                }
                else{
                    ffMutableLiveData.stopPing();
                }
            }
        });
    }

    private void nuevoPing() {

        String direccion = tilUrl.getEditText().getText().toString();
        String numeroPaquetes = tilNumeroPaquetes.getEditText().getText().toString();

        if (!TextUtils.isEmpty(direccion)) {
            if (!TextUtils.isDigitsOnly(numeroPaquetes) || TextUtils.isEmpty(numeroPaquetes)) {
                numeroPaquetes = NUMERO_DE_PAQUETES_POR_DEFECTO;
            }
            ffMutableLiveData.iniciarPing(direccion, numeroPaquetes);
        } else {
            Toast.makeText(requireActivity(), "Ingrese una dirección ip o una url", Toast.LENGTH_SHORT).show();
        }
    }
}