package com.stripe.example.activity;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.stripe.example.R;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.compat.AsyncTask;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.example.dialog.ErrorDialogFragment;
import com.stripe.example.dialog.ProgressDialogFragment;
import com.stripe.example.PaymentForm;
import com.stripe.example.TokenList;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;


public class PaymentActivity extends FragmentActivity {

    /*
     * Change this to your publishable key.
     *
     * You can get your key here: https://manage.stripe.com/account/apikeys
     */
    //public static final String PUBLISHABLE_KEY = "pk_test_6pRNASCoBOKtIshFeQd4XMUh";

	  public static final String PUBLISHABLE_KEY = "pk_test_HYj7xln0weWeiT8b0SmPNhXW";

    private ProgressDialogFragment progressFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_activity);
        
        TextView tv=(TextView)findViewById(R.id.paymentAmtNum);
    	tv.setText(" 6.00 Dollars");

        progressFragment = ProgressDialogFragment.newInstance(R.string.progressMessage);
    }
    
    class ChargeAction extends AsyncTask<Token, Void, Void> {

        protected Void doInBackground(Token... token) {
            try {
            	
            	com.stripe.Stripe.apiKey="sk_test_B5SR6KPbcaO4p3l736tq9AwA";

            	Map<String, Object> chargeParams = new HashMap<String, Object>();
            	chargeParams.put("amount", 600);
            	
            	
            	
            	chargeParams.put("currency", "usd");
            	Map<String, Object> sourceParams = new HashMap<String, Object>();
            	sourceParams.put("number", "4242424242424242");
            	sourceParams.put("exp_month", 3);
            	sourceParams.put("exp_year", 2016);
            	sourceParams.put("cvc", "314");
            	chargeParams.put("source", sourceParams);
            	chargeParams.put("description", "Charge for test@example.com");

 
					Charge.create(chargeParams);
					
				} catch (AuthenticationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidRequestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (APIConnectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CardException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (APIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			return null;
        }
    }

    public void saveCreditCard(PaymentForm form) {

        Card card = new Card(
                form.getCardNumber(),
                form.getExpMonth(),
                form.getExpYear(),
                form.getCvc());

        boolean validation = card.validateCard();
        if (validation) {
            startProgress();
           new Stripe().createToken(
                    card,
                    PUBLISHABLE_KEY,
                    new TokenCallback() {
                    public void onSuccess(Token token) {
                    
                    	new ChargeAction().execute(token);
               
                        getTokenList().addToList(token);
                        finishProgress();
                        }
                    public void onError(Exception error) {
                            handleError(error.getLocalizedMessage());
                            finishProgress();
                        }
                    });
        } else if (!card.validateNumber()) {
        	handleError("The card number that you entered is invalid");
        } else if (!card.validateExpiryDate()) {
        	handleError("The expiration date that you entered is invalid");
        } else if (!card.validateCVC()) {
        	handleError("The CVC code that you entered is invalid");
        } else {
        	handleError("The card details that you entered are invalid");
        }
    }

    private void startProgress() {
        progressFragment.show(getSupportFragmentManager(), "progress");
    }

    private void finishProgress() {
        progressFragment.dismiss();
    }

    private void handleError(String error) {
        DialogFragment fragment = ErrorDialogFragment.newInstance(R.string.validationErrors, error);
        fragment.show(getSupportFragmentManager(), "error");
    }

    private TokenList getTokenList() {
        return (TokenList)(getSupportFragmentManager().findFragmentById(R.id.token_list));
    }
}
