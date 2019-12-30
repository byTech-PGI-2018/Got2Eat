package bytech.got2eat.chat;

import android.content.Intent;

import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.Date;

import bytech.got2eat.recipe.RecipeShow;
import bytech.got2eat.restaurants.Restaurantes;
import bytech.got2eat.home.Home;

public class Chat {

    private MessagesList messagesList;
    private MessagesListAdapter<Message> adapter;
    private ArrayList<Message> messages = new ArrayList<>();
    private final Home homeInstance;
    private Author user, bot;

    public Chat(Home homeInstance){
        this.homeInstance = homeInstance;
    }

    public Chat(Home homeInstance, Author user, Author bot){
        this.homeInstance = homeInstance;
        this.user = user;
        this.bot = bot;
    }


    public void setUser(Author user){
        this.user = user;
    }

    public void setBot(Author bot){
        this.bot = bot;
    }



    public void setMessagesList(MessagesList list) {this.messagesList = list;}

    public void setMessagesListAdapter(MessagesListAdapter<Message> adapter) {
        this.adapter = adapter;
        this.messagesList.setAdapter(adapter);

        /*On a message tap, if it's a recipe with a valid firestore ID, open up a RecipeShow activity*/
        adapter.setOnMessageClickListener(new MessagesListAdapter.OnMessageClickListener<Message>() {
            @Override
            public void onMessageClick(Message message) {
                if (message.getFirestoreId() != null) {
                    Intent intent = new Intent(homeInstance, RecipeShow.class);
                    intent.putExtra("firestoreId", message.getFirestoreId());
                    homeInstance.startActivity(intent);
                }
            }
        });
    }


    public void updateMessagesList(ArrayList<Message> messages){
        this.messages.addAll(messages);
    }

    /*Create a chat response in the user's side of the conversation*/
    public void addMessage(Message messageObj){
        adapter.addToStart(messageObj, true);
        /*Also, save the message in the log*/
        messages.add(messageObj);
    }

    /*Create a chat response, or multiple responses in case of found recipes, in the bot's side of the conversation*/
    public void respond(String message) {
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
            Intent intent = new Intent(this.homeInstance, Restaurantes.class);
            homeInstance.startActivity(intent);
        }

    }

    public ArrayList<Message> retrieveAllMessages(){
        return messages;
    }
}
