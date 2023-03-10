package net.lrivas.practicageolocalizacion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
   private Button Ubicacion, compartir, mapa;
   private TextView tvLatitud,tvlonguitud,tvDireccion;
    public static final int CODE_UBICATION=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Ubicacion = findViewById(R.id.btnObtener);
        tvLatitud = findViewById(R.id.tvlatitud);
        tvlonguitud = findViewById(R.id.tvLonguitud);
        tvDireccion = findViewById(R.id.tvDireccion);

        mapa = findViewById(R.id.btnMapa);
        mapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapa = new Intent(MainActivity.this, Mapa.class);
                mapa.putExtra("longitud", Double.parseDouble(tvlonguitud.getText().toString()));
                mapa.putExtra("latitud", Double.parseDouble(tvLatitud.getText().toString()));
                startActivity(mapa);
            }
        });

        compartir = findViewById(R.id.btnCompartir);
        compartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentWhatsApp = new Intent(Intent.ACTION_SEND);
                intentWhatsApp.setType("text/plain");
                intentWhatsApp.setPackage("com.whatsapp");
                String latitud = tvLatitud.getText().toString();
                String longitud = tvlonguitud.getText().toString();
                String url = "https://maps.google.com/?q="+latitud+","+longitud+"";
                intentWhatsApp.putExtra(Intent.EXTRA_TEXT, "Ubicación actual: "+url);
                startActivity(intentWhatsApp);
            }
        });

        Ubicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerUbicacion();
            }
        });
    }

    private void obtenerUbicacion() {
        verificarPermisoUbicacion();
    }

    private void verificarPermisoUbicacion() {
        if(
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    CODE_UBICATION
            );
        }else {
            iniciarUbicacion();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODE_UBICATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarUbicacion();
                return;
            }
        }
    }

    private void iniciarUbicacion() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion localizacion = new Localizacion();
        localizacion.setMainActivity(this);

        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }

        if(
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    CODE_UBICATION
            );
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, localizacion);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, localizacion);
        Toast.makeText(this, "Localizacion Iniciada", Toast.LENGTH_SHORT).show();
        tvLatitud.setText(null);
        tvlonguitud.setText(null);
        tvDireccion.setText(null);
    }
    private void obtenerDireccion(Location location) {
        if (location.getLatitude() != 0 && location.getLongitude() != 0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        1
                );

                if (!list.isEmpty()) {
                    Address direccion  = list.get(0);
                    tvDireccion.setText(direccion.getAddressLine(0));
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public class Localizacion implements LocationListener {
        MainActivity mainActivity;

        public void setMainActivity(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onLocationChanged(@NonNull Location location) {
            tvLatitud.setText(String.valueOf(location.getLatitude()));
            tvlonguitud.setText(String.valueOf(location.getLongitude()));
            this.mainActivity.obtenerDireccion(location);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            LocationListener.super.onProviderEnabled(provider);
            Log.i("Status GPS", "GPS Activado");
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            LocationListener.super.onProviderDisabled(provider);
            Log.i("Status GPS", "GPS Desactivado");
        }

    }
}