package bytech.got2eat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
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

public class Home extends AppCompatActivity implements AIListener, NavigationView.OnNavigationItemSelectedListener{

    private Author user;
    private Author bot;
    private TextInputLayout userInput;
    private Button inputEnter;
    private List<Message> messages = new ArrayList<>();
    private AIDataService aiService;
    private AIRequest aiRequest;
    private Home thisInstance = this;
    private MessagesListAdapter<Message> adapter = new MessagesListAdapter<>(FirebaseAuth.getInstance().getUid(), null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

	NavigationView navView;
	TextView navDisplayName;

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                navView = findViewById(R.id.nav_view);
                navView.setNavigationItemSelectedListener(thisInstance);
                navDisplayName = navView.findViewById(R.id.nav_header_textView);
                navDisplayName.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            }
        }, 300);

        final AIConfiguration config = new AIConfiguration("bd09387ec42144bd9dbf3ea09141f6fd",
                AIConfiguration.SupportedLanguages.Portuguese,
                AIConfiguration.RecognitionEngine.System);

        aiService = new AIDataService(config);

        MessagesList messagesList = findViewById(R.id.messagesList);
        messagesList.setAdapter(adapter);

        userInput = findViewById(R.id.user_input_layout);

        inputEnter = findViewById(R.id.input_enter_button);
        inputEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Validate input
                final String message = userInput.getEditText().getText().toString();
                if (validateInput(message)==0){
                    Message messageObj = new Message(message, FirebaseAuth.getInstance().getUid(), new Date(), user);

                    messages.add(messageObj);
                    adapter.addToStart(messageObj, true);

                    //Send HTTP request to Dialogflow
                    aiRequest = new AIRequest();
                    aiRequest.setQuery(message);
                    new DialogTask(aiService, aiRequest, thisInstance).execute(aiRequest);

                }
            }
        });

        user = new Author(FirebaseAuth.getInstance().getUid(),FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),null);
        bot = new Author("bot", "bot", null);
    }

    private void respond(String message) {
        Message obj = new Message(message, "bot", new Date(), bot);
        messages.add(obj);
        adapter.addToStart(obj, true);
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
                Toast.makeText(thisInstance, R.string.signed_out, Toast.LENGTH_LONG).show();
                finish();
                return true;
	    default:
		//Satisfy codacy
                return true;
        }
        return false;
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
