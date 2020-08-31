## IAPlayer

IAPlayer is a media player that interacts with users. You can make your video's story change with your response. IAPlayer has all the capabilites of the Exoplayer and communicates with the server through SocketService. It was UIPlayer at first, but I changed it to IAPlayer because it could cause confusion with the user interface.



### Project Scenario

![image-20200831160304195](/Users/owonseog/Library/Application Support/typora-user-images/image-20200831160304195.png)

1. Start IAPlayer
2. Foreground : specfic movie playback 
   Background : server connet (socket server)
3. Wait Event
4. Require a user to response when an event occurs
5. Send a user's response to server
6. server socket change movie

### Component Design

![image-20200831155923692](/Users/owonseog/Library/Application Support/typora-user-images/image-20200831155923692.png)

- IAPlayer : Interaction Player
  IAPlayer has mainPlayer and playerlist.

  mainPlayer is SimpleIAPlayer who is in charge of the actual player

  playerlist is a temporary storage player list to solve buffering problems

- SimpleIAPlayer : Exoplayer child and has customizing player.EventListener 

- SocketService : connect to server sockets in the background

- IAMessage : IAMessage has event-related information ex) EventTime, url, title...

### Using IAPlayer

The same environment for using Exoplayer

### Step 1

Create IAPlayer instance.

```
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
```

### Step 2

Prepare media sample and set host and port.

```
// Media sample uri
Uri uri = Uri.parse(URL);

// Component for requesting data (HTTP, uri ..)
DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this,"example-test"));

// Media sample sources required for http live streaming
HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
iaPlayer.setDataSourceFactory(dataSourceFactory);
iaPlayer.setHostAndPort("localhost",5001);
```

### Step3

Customize IAListener and connect SocketService.

```
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


            }
        },
        hlsMediaSource
);
iaPlayer.connect(this, SocketService.class);
```

You can also customize the socketservice that is responsible for server communication.

### End

I'm still junior developer. I'm going to grow into an Android developer. I know is a way that not right to be inefficient and library iaplayer But I have made based on knowledge of the library. Thanks very much a drip if you would give the advice you gave me an efficient development. ( ex) pull request ..)

Thank you for reading my readme.



