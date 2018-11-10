package bytech.got2eat;

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

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class Home extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextInputLayout userInput;
    private Button inputEnter;
    private MessageListAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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

                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Respond
                            respond(message);
                        }
                    }, 1000);
                }
            }
        });
    }

    private void respond(String message) {
        Message obj = new Message("Hello", "bot", 1232, false);
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
}
