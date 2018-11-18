package bytech.got2eat;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class Home extends AppCompatActivity implements AIListener{

    private RecyclerView recyclerView;
    private TextInputLayout userInput;
    private Button inputEnter;
    private MessageListAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>();
    private AIDataService aiService;
    private AIRequest aiRequest;
    private Home thisInstance = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final AIConfiguration config = new AIConfiguration("bd09387ec42144bd9dbf3ea09141f6fd",
                AIConfiguration.SupportedLanguages.Portuguese,
                AIConfiguration.RecognitionEngine.System);

        aiService = new AIDataService(config);

        recyclerView = findViewById(R.id.chat_list);
        userInput = findViewById(R.id.user_input_layout);
        inputEnter = findViewById(R.id.input_enter_button);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);
        recyclerView.setItemAnimator(new SlideInUpAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(messageAdapter);

        messageAdapter = new MessageListAdapter(this, messages);
        AlphaInAnimationAdapter anim = new AlphaInAnimationAdapter(messageAdapter);
        recyclerView.setAdapter(new SlideInBottomAnimationAdapter(anim));

        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (!messages.isEmpty()){
                    if (bottom < oldBottom) {
                        recyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.smoothScrollToPosition(
                                        recyclerView.getAdapter().getItemCount() - 1);
                            }
                        }, 100);
                    }
                }
            }
        });

        inputEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Validate input
                final String message = userInput.getEditText().getText().toString();
                if (validateInput(message)==0){
                    Message messageObj = new Message(message, "self", 12332, true);

                    messages.add(messageObj);
                    messageAdapter.notifyItemInserted(messages.size()-1);
                    messageAdapter.notifyItemRangeChanged(messages.size()-2, 0);
                    AlphaInAnimationAdapter anim = new AlphaInAnimationAdapter(messageAdapter);
                    recyclerView.setAdapter(new SlideInBottomAnimationAdapter(anim));

                    //Send HTTP request to Dialogflow
                    aiRequest = new AIRequest();
                    aiRequest.setQuery(message);
                    new DialogTask(aiService, aiRequest, thisInstance).execute(aiRequest);


                    /*Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Respond
                            respond(message);
                        }
                    }, 1000);*/
                }
            }
        });
    }

    private void respond(String message) {
        Message obj = new Message(message, "bot", 1232, false);
        messages.add(obj);
        messageAdapter.notifyItemInserted(messages.size()-1);
        messageAdapter.notifyItemRangeChanged(messages.size()-2, 0);
        AlphaInAnimationAdapter anim = new AlphaInAnimationAdapter(messageAdapter);
        recyclerView.setAdapter(new SlideInBottomAnimationAdapter(anim));
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
