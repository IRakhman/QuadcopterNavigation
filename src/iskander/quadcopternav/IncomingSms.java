package iskander.quadcopternav;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class IncomingSms extends BroadcastReceiver {
	// Get the object of SmsManager
	private String message;
	final SmsManager sms = SmsManager.getDefault();
	public void onReceive(Context context, Intent intent) {
		// Retrieves a map of extended data from the intent.
		final Bundle bundle = intent.getExtras();
		try {
			if (bundle != null) {
				final Object[] pdusObj = (Object[]) bundle.get("pdus");
				for (int i = 0; i < pdusObj.length; i++) {
					SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
					String phoneNumber = currentMessage.getDisplayOriginatingAddress();
					String senderNum = phoneNumber;
					message = currentMessage.getDisplayMessageBody();
					Log.i("SmsReceiver", "senderNum: "+ senderNum + "; message: " + message);
					int duration = Toast.LENGTH_LONG;
					Toast toast = Toast.makeText(context, "senderNum: "+ senderNum + ", message: " + message, duration);
					toast.show();
				}
			} // bundle is null
		} catch (Exception e) {
			Log.e("SmsReceiver", "Exception smsReceiver" +e);
		}
		
	}
	
	public String getMessage() {
		return message;
	}
}






