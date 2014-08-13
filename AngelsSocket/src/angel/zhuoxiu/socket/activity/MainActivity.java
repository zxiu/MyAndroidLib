package angel.zhuoxiu.socket.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import angel.zhuoxiu.socket.R;

public class MainActivity extends Activity {
	Button btnInfo;
	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v == btnInfo) {
				startActivity(new Intent(MainActivity.this, InfoActivity.class));
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		btnInfo = (Button) findViewById(R.id.btn_info);
		btnInfo.setOnClickListener(onClickListener);
	}
}
