package ows.boostcourse.uiplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import ows.boostcourse.uiplayer.databinding.ActivityMainBinding;
import ows.boostcourse.iaplayer.IAPlayer;
import ows.boostcourse.iaplayer.IAListener;
import ows.boostcourse.iaplayer.IAMeesage;
import ows.boostcourse.iaplayer.SocketService;

public class MainActivity extends AppCompatActivity{

    public static final String URL = "https://multiplatform-f.akamaihd.net/i/multi/will/bunny/big_buck_bunny_,640x360_400,640x360_700,640x360_1000,950x540_1500,.f4v.csmil/master.m3u8";
    ActivityMainBinding binding;

    IAPlayer iaPlayer;      // 실행할 IAPlayer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Component that set the network bandwidth
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter.Builder().build();

        // AdaptiveTrackSelection Component
        AdaptiveTrackSelection.Factory adaptiveTrackSelection = new AdaptiveTrackSelection.Factory(defaultBandwidthMeter);

        // Set default track section
        DefaultTrackSelector defaultTrackSelector = new DefaultTrackSelector(adaptiveTrackSelection);

        // Control media buffering
        LoadControl loadControl = new DefaultLoadControl();

        // Component acting as rendering
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this);

        // Create IAPlayer
        iaPlayer = new IAPlayer(renderersFactory,defaultTrackSelector,loadControl,null);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        binding.exoplayer.setPlayer(iaPlayer.getIAPlayer());

        // Media sample uri
        Uri uri = Uri.parse(URL);

        // Component for requesting data (HTTP, uri ..)
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this,"example-test"));

        // Media sample sources required for http live streaming
        HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        iaPlayer.setDataSourceFactory(dataSourceFactory);
        iaPlayer.setHostAndPort("localhost",5001);

        // IAPlayer prepare
        iaPlayer.prepare(

                // Customizeing IAListener
                new IAListener() {

                    // Callback method invoked when an event occurs.
                    @Override
                    public void onConnet() {

                    }

                    // Callback method invoked when a user responses.
                    @Override
                    public void onUserSelect(final IAMeesage iaMeesage) {

                        final Dialog dialog = new Dialog(MainActivity.this);
                        View view = getLayoutInflater().inflate(R.layout.dialog,null);

                        Button button = view.findViewById(R.id.button);
                        button.setText(iaMeesage.getTitle()[0]);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                iaPlayer.decidePlayer(0,iaMeesage.getNextId()[0]);
                                binding.exoplayer.setPlayer(iaPlayer.getIAPlayer());
                                dialog.dismiss();
                            }
                        });

                        Button button2 = view.findViewById(R.id.button2);
                        button2.setText(iaMeesage.getTitle()[1]);
                        button2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                iaPlayer.decidePlayer(1,iaMeesage.getNextId()[1]);
                                binding.exoplayer.setPlayer(iaPlayer.getIAPlayer());
                                dialog.dismiss();
                            }
                        });
                        dialog.setContentView(view);

                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        lp.copyFrom(dialog.getWindow().getAttributes());
                        lp.width = 1000;
                        lp.height = 1000;
                        dialog.getWindow().setAttributes(lp);

                        dialog.show();

                    }
                },
                hlsMediaSource
        );
        iaPlayer.connect(this, SocketService.class);


    }

}
