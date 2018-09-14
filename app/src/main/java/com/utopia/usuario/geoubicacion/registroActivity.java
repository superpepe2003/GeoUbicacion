package com.utopia.usuario.geoubicacion;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.icu.util.Output;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.Marshal;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.MarshalHashtable;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;

public class registroActivity extends AppCompatActivity {

    final String NAMESPACE = "http://tempuri.org/";
    final String URL="http://geolocaliza.ddns.net/WcfGeoLocation/WSGeoUbicacion.svc";
    final String METHOD_NAME = "GrabarU";
    final String SOAP_ACTION = "http://tempuri.org/IWSGeolizacion/GrabarU";

    ImageButton btnImage;
    String foto, dir;
    TextInputLayout txtAdminLayout, txtNombreLayout, txtUserLayout, txtPassLayout;
    TextInputEditText txtAdmin, txtNombre, txtUser, txtPass;
    Button btnGuardar, btnCancelar;
    CheckBox chkSupervisa;
    Boolean isFoto =false;

    Usuario _user = new Usuario();

    String base64String;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        btnImage= (ImageButton)findViewById(R.id.btnImagen);
        chkSupervisa = (CheckBox) findViewById(R.id.chkSupervisado);
        txtAdminLayout = (TextInputLayout) findViewById(R.id.txtUserAdministradorLayout);
        txtAdmin = (TextInputEditText)findViewById(R.id.txtUserAdministrador_registro);
        txtNombre=findViewById(R.id.txtNombre_registro);
        txtUser=findViewById(R.id.txtUser_registro);
        txtPass=findViewById(R.id.txtPass_registro);
        txtNombreLayout=findViewById(R.id.txtNombreLayout);
        txtUserLayout= findViewById(R.id.txtUserLayout);
        txtPassLayout=findViewById(R.id.txtPassLayout);
        btnGuardar= findViewById(R.id.btnGuardar_Usuario);
        btnCancelar=findViewById(R.id.btnCancelar_Usuario);

        CargarEventos();
        CargarImagenes();
        UsarChecked();

    }

    private void CargarEventos()
    {
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Validar())
                {
                    _user.nombre=txtNombre.getText().toString();
                    _user.pass=Integer.parseInt(txtPass.getText().toString());
                    _user.usuario=txtUser.getText().toString();
                    _user.usuarioAdmin=txtAdmin.getText().toString();

                    miTask mTask= new miTask();
                    mTask.execute();
                }
            }
        });

        txtUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txtUserLayout.setError("");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        txtPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txtPassLayout.setError("");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        txtNombre.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txtNombreLayout.setError("");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private Boolean Validar()
    {
        Boolean f =true;
        if(!(txtUser.getText().length()>0))
        {
            txtUserLayout.setError(getResources().getString(R.string.user) + " " + getResources().getString(R.string.error_registro1));
            f=false;
        }
        if(!(txtNombre.getText().length()>0))
        {
            txtNombreLayout.setError(getResources().getString(R.string.nombre) + " " + getResources().getString(R.string.error_registro1));
            f=false;
        }
        if(!(txtPass.getText().length()>0))
        {
            txtPassLayout.setError(getResources().getString(R.string.pass) + " " + getResources().getString(R.string.error_registro1));
            f=false;
        }
        if(txtPass.getText().length()<6)
        {
            txtPassLayout.setError(getResources().getString(R.string.pass) + " " + getResources().getString(R.string.error_registro2));
            f=false;
        }
        return f;
    }

    private void UsarChecked()
    {
        chkSupervisa.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    txtAdminLayout.setVisibility(View.VISIBLE);
                }
                else
                {
                    txtAdminLayout.setVisibility(View.INVISIBLE);
                    txtAdmin.setText("");
                }
            }
        });
    }

    private void CargarImagenes()
    {
        Integer aleatorio= new Double(Math.random()*100).intValue();


        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolder/";
        File newdir = new File(dir);
        if (!newdir.exists()) {
            newdir.mkdir();
        }

        foto = dir + "imagen" + aleatorio + ".jpg";

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //Uri out = Uri.fromFile(new File(foto));
                File newfile = new File(foto);
                try {
                    newfile.createNewFile();
                }
                catch (IOException e)
                {
                }
                Uri out = FileProvider.getUriForFile(registroActivity.this, "com.utopia.usuario.geoubicacion", newfile);
                in.putExtra(MediaStore.EXTRA_OUTPUT, out);
                startActivityForResult(in, 2);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        File file= new File(foto);
        if(file.exists()) {
            Bitmap originalBitmap = BitmapFactory.decodeFile(foto);
            RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(),originalBitmap);
            roundDrawable.setCornerRadius(originalBitmap.getHeight());

            btnImage.setImageDrawable(roundDrawable);
            btnImage.setRotation(-90);
            isFoto=true;
            //base64String = imagenToByte();
        }
    }

    private class miTask extends AsyncTask<Void,Void,Integer>{

        //int Operacion=1;
        @Override
        protected Integer doInBackground(Void... voids) {
           int a= GuardarUsuario();
            return a;
        }

        @Override
        protected void onPostExecute(Integer aResult) {
            if(aResult==0) {
                Toast.makeText(registroActivity.this, "Usuario Grabado", Toast.LENGTH_LONG).show();
                finish();
            }
            if(aResult==-1)
                Toast.makeText(registroActivity.this, "Error de conexcion al Grabar", Toast.LENGTH_LONG).show();
            else if(aResult==1)
                txtUserLayout.setError("Usuario ya Existe!");
            else if(aResult==2)
                txtAdminLayout.setError("Usuario no existe");
            super.onPostExecute(aResult);

        }
    }

    public int GuardarUsuario()
    {
        try {
            //cargarTodosActivity.this.txtCarga.setText("Conectando");
            // Modelo el request
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            String fot1 = imagenToByte();

            Escribir(fot1);
            request.addProperty("id", 0);
            request.addProperty("user",_user.usuario);
            request.addProperty("nombre",_user.nombre);
            request.addProperty("pass",_user.pass);
            request.addProperty("userAdmin", _user.usuarioAdmin + "");
            request.addProperty("foto", fot1);


            // Modelo el Sobre
            SoapSerializationEnvelope sobre = new SoapSerializationEnvelope(SoapEnvelope.VER11);

            //sobre.implicitTypes = true;
            sobre.dotNet = true;

            sobre.encodingStyle = SoapSerializationEnvelope.ENC;
            //new MarshalHashtable().register(sobre);
            //new MarshalBase64().register(sobre);


            sobre.setOutputSoapObject(request);

            //MarshalDouble md = new MarshalDouble();
            //md.register(sobre);


            // Modelo el transporte
            HttpTransportSE transporte = new HttpTransportSE(URL);

            //transporte.debug=true;
            // Llamada
            transporte.call(SOAP_ACTION, sobre);

            // Resultado
            String resultado = sobre.getResponse().toString();


            //Log.i("Resultado", resultado.toString());

            return Integer.parseInt(resultado);

            //String re= transporte.requestDump;

            //Log.e("Resultado", re);

        } catch (Exception e) {
            //Log.e("ERROR", e.getMessage());
            return -1;
        }
    }

    public void Escribir(String texto)
    {
        try {
            File fav = new File(dir + "fichero.txt");
            OutputStreamWriter file = new OutputStreamWriter(new FileOutputStream(fav));

            file.write(texto);

            file.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public String imagenToByte()
    {
        Bitmap bm = BitmapFactory.decodeFile(foto);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,50,baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b,Base64.DEFAULT);
    }

//    class CmarshalBase64 implements Marshal {
//        public static Class BYTE_ARRAY_CLASS = new byte[0].getClass();
//
//        public Object readInstance(XmlPullParser parser, String namespace, String name, PropertyInfo expected)
//                throws IOException, XmlPullParserException {
//            return Base64.decode(parser.nextText());
//        }
//
//        public void writeInstance(XmlSerializer writer, Object obj) throws IOException {
//            writer.text(Base64.encode(obj));
//        }
//
//        public void register(SoapSerializationEnvelope cm) {
//            cm.addMapping(cm.xsd, "base64Binary", MarshalBase64.BYTE_ARRAY_CLASS, this);
//            cm.addMapping(SoapEnvelope.ENC, "base64", MarshalBase64.BYTE_ARRAY_CLASS, this);
//        }
//    }

}
