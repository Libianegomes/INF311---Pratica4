package br.ufv.inf311.pratica04leituras;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;

public class LanternaHelper {

    private CameraManager cameraManager;
    private String cameraId;

    public LanternaHelper(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                if (cameraManager == null) {
                    return;
                }

                for (String id : cameraManager.getCameraIdList()) {
                    Boolean flashDisponivel = cameraManager
                            .getCameraCharacteristics(id)
                            .get(CameraCharacteristics.FLASH_INFO_AVAILABLE);

                    if (Boolean.TRUE.equals(flashDisponivel)) {
                        cameraId = id;
                        break;
                    }
                }
            } catch (CameraAccessException | SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void ligar() {
        alterarEstadoLanterna(true);
    }

    public void desligar() {
        alterarEstadoLanterna(false);
    }

    private void alterarEstadoLanterna(boolean ligar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cameraManager != null && cameraId != null) {
            try {
                cameraManager.setTorchMode(cameraId, ligar);
            } catch (CameraAccessException | IllegalArgumentException | SecurityException e) {
                e.printStackTrace();
            }
        }
    }
}
