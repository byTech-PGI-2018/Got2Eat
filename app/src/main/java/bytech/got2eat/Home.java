package bytech.got2eat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.shape.CircleShape;

public class Home extends AppCompatActivity implements AIListener, NavigationView.OnNavigationItemSelectedListener{
    private static final String TAG = "Home";
    private DrawerLayout drawer;
    private NavigationView navView = null;
    private TextView navDisplayName = null;
    private TextView showcaseTextViewTarget;
    private Author user;
    private Author bot;
    private EditText userInput;
    private ImageView inputEnter;
    private ArrayList<Message> messages = new ArrayList<>();
    private AIDataService aiService;
    private AIRequest aiRequest;
    private Home thisInstance = this;
    private FirebaseFirestore db;
    private ImageLoader imageLoader = new ImageLoader() {
        @Override
        public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
            Picasso.with(getApplicationContext()).load(url).into(imageView);
        }
    };
    private String SHOWCASE_ID = "1";
    private MessagesListAdapter<Message> adapter = new MessagesListAdapter<>(FirebaseAuth.getInstance().getUid(), imageLoader);
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (FirebaseAuth.getInstance() == null){
            Intent intent = new Intent(thisInstance, Login.class);
            startActivity(intent);
            finish();
            return;
        }
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Retrive data (if it exists)
        mPrefs = getPreferences(MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("History", "");
        History history = gson.fromJson(json, History.class);
        Log.d(TAG, "History: " + history);
        if (history != null && history.history!=null && !history.history.isEmpty()){
            Log.d(TAG, "Adding history");
            adapter.addToEnd(history.history, true);
        }

        db = FirebaseFirestore.getInstance();

        showcaseTextViewTarget = findViewById(R.id.drawer_slide_hint_pos);

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawer = findViewById(R.id.drawer);
                navView = findViewById(R.id.nav_view);
                navView.setNavigationItemSelectedListener(thisInstance);
                navDisplayName = navView.findViewById(R.id.nav_header_textView);
                navDisplayName.setText("");
                db.collection("users").document(FirebaseAuth.getInstance().getUid())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();
                                    navDisplayName.setText((String)document.get("firstname"));
                                }
                                else{
                                    navDisplayName.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                                }
                            }
                        });
            }
        }, 300);

        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                new MaterialShowcaseView.Builder(thisInstance)
                        .setTarget(showcaseTextViewTarget)
                        .setDismissText("ENTENDIDO")
                        .setShape(new CircleShape())
                        .setContentText("Para abrir a gaveta, deslize para a direita a partir do lado esquerdo")
                        .setDelay(1500) // optional but starting animations immediately in onCreate can make them choppy
                        .singleUse(SHOWCASE_ID) // provide a unique ID used to ensure it is only shown once
                        .show();
            }
        }, 2000);

        final AIConfiguration config = new AIConfiguration("bd09387ec42144bd9dbf3ea09141f6fd",
                AIConfiguration.SupportedLanguages.Portuguese,
                AIConfiguration.RecognitionEngine.System);

        aiService = new AIDataService(config);

        MessagesList messagesList = findViewById(R.id.messagesList);
        messagesList.setAdapter(adapter);
        adapter.setOnMessageClickListener(new MessagesListAdapter.OnMessageClickListener<Message>() {
            @Override
            public void onMessageClick(Message message) {
                if (message.getFirestoreId() != null) {
                    Intent intent = new Intent(thisInstance, RecipeShow.class);
                    intent.putExtra("firestoreId", message.getFirestoreId());
                    startActivity(intent);
                }
            }
        });

        userInput = findViewById(R.id.user_input);

        inputEnter = findViewById(R.id.input_enter_button);
        inputEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Validate input
                final String message = userInput.getText().toString();
                if (validateInput(message)==0){
                    Message messageObj = new Message(message, FirebaseAuth.getInstance().getUid(), new Date(), user);

                    messages.add(messageObj);
                    adapter.addToStart(messageObj, true);

                    //Save in database
                    db.collection("users").document(FirebaseAuth.getInstance().getUid())
                            .update("logs", FieldValue.arrayUnion("" + message));

                    //Send HTTP request to Dialogflow
                    aiRequest = new AIRequest();
                    aiRequest.setQuery(message);
                    new DialogTask(aiService, aiRequest, thisInstance).execute(aiRequest);
                    userInput.setText("");

                }
            }
        });

        user = new Author(FirebaseAuth.getInstance().getUid(),FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),null);
        Uri uri = Uri.parse("android.resource://" + getApplicationContext().getPackageName()+"/"+R.drawable.mascote);
        bot = new Author("bot", "bot", uri.toString());

        ImageView helpButton = findViewById(R.id.helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater)
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popupwindow, null);


                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        History history = new History(messages);
        Gson gson = new Gson();
        String json = gson.toJson(history);
        prefsEditor.putString("History", json);
        prefsEditor.apply();
        super.onPause();
    }

    private void respond(String message) {
        //Find if there are recipes
        if(!message.matches("Vou pesquisar restaurantes próximos de si!")){
            String[] tokens = message.split("_");
            if (tokens.length > 1){
                //There is at least one recipe
                //Send initial
                Message initial = new Message(tokens[0], "bot", new Date(), bot);
                adapter.addToStart(initial, true);

                for (int i=1; i<tokens.length; i+=2){
                    //Send separate messages and set their onClick to a recipe
                    final Message obj = new Message(tokens[i+1], "bot", new Date(), bot, tokens[i]);
                    messages.add(obj);
                    adapter.addToStart(obj, true);
                }
            }
            else{
                Message obj = new Message(message, "bot", new Date(), bot);
                messages.add(obj);
                adapter.addToStart(obj, true);
            }
        }else{
            Intent intent = new Intent(this.thisInstance,Restaurantes.class);
            startActivity(intent);
        }

    }

    private int validateInput(String message){
        if (message.isEmpty()){
            userInput.setError(getString(R.string.empty_username));
            return 1;
        }
        else{
            userInput.setError(null);
            return 0;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_item_sign_out:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(thisInstance, Login.class);
                startActivity(intent);
                drawer.closeDrawers();
                Toast.makeText(thisInstance, R.string.signed_out, Toast.LENGTH_LONG).show();
                finish();
                return true;
            case R.id.nav_item_saved_recipes:
                intent = new Intent(thisInstance, SavedRecipes.class);
                startActivity(intent);
                drawer.closeDrawers();
                return true;
            case R.id.nav_item_about_us:
                intent = new Intent(thisInstance, aboutUs.class);
                startActivity(intent);
                drawer.closeDrawers();
                return true;
            default:
		        //Satisfy codacy
                return true;
        }
    }

    @Override
    public void onResult(AIResponse result) {
    }

    @Override
    public void onError(AIError error) {
    }

    @Override
    public void onAudioLevel(float level) {
    }

    @Override
    public void onListeningStarted() {
    }

    @Override
    public void onListeningCanceled() {
    }

    @Override
    public void onListeningFinished() {
    }

    private static class DialogTask extends AsyncTask<AIRequest,Void,AIResponse>{
        private AIDataService aiService;
        private AIRequest aiRequest;
        private static Home homeClass;

        DialogTask(AIDataService aiService, AIRequest aiRequest, Home homeClass){
            this.aiService = aiService;
            this.aiRequest = aiRequest;
            DialogTask.homeClass = homeClass;
        }

        @Override
        protected AIResponse doInBackground(AIRequest... aiRequests) {
            final AIRequest request = aiRequests[0];
            try {
                final AIResponse response = aiService.request(aiRequest);
                return response;
            } catch (AIServiceException e) {
            }
            return null;
        }
        @Override
        protected void onPostExecute(AIResponse response) {
            if (response != null) {

                Result result = response.getResult();
                String reply = result.getFulfillment().getSpeech();
                homeClass.respond(reply);
            }
        }
    }
}
