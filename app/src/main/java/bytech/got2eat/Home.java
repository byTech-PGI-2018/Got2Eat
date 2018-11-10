package bytech.got2eat;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

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
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);

        messageAdapter = new MessageListAdapter(this, messages);

        inputEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Validate input
                String message = userInput.getEditText().getText().toString();
                if (validateInput(message)==0){
                    Message messageObj = new Message(message, "self", 12332, true);

                    messages.add(messageObj);
                    messageAdapter.notifyItemChanged(messages.size()-1);
                    recyclerView.setAdapter(messageAdapter);

                    //Respond
                    respond(message);
                }
            }
        });
    }

    private void respond(String message) {
        Message obj = new Message("Hello", "bot", 1232, false);
        messages.add(obj);
        messageAdapter.notifyItemChanged(messages.size()-1);
        recyclerView.setAdapter(messageAdapter);
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
