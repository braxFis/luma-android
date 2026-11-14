package se.luma.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.media.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    final int bg=0xff101b20, panel=0xff1c2b31, white=0xfff0f3e9, muted=0xffa6b9b9, mint=0xffc9e8ba;
    final String[] names={"Havets andetag","Regn mot fönstret","Skogens stillhet","När kvällen landar"};
    final String[] details={"Vågor · en stund för dig","Mjukt regn · hitta ditt fokus","Skogsbrus · kom ner i varv","Varm atmosfär · vila tankarna"};
    int selected=0, filter=0; boolean playing=false, favoritesOnly=false;
    AudioTrack track; AudioManager audio; AudioFocusRequest focus;
    LinearLayout root, cards; TextView title, subtitle, timerLabel; Button play, heart;
    Handler handler=new Handler(Looper.getMainLooper()); long deadline=0;
    android.content.SharedPreferences prefs;
    final Runnable ticker=new Runnable(){public void run(){
        if(deadline>0){long left=deadline-SystemClock.elapsedRealtime(); if(left<=0){deadline=0; pause();} timerLabel.setText(deadline==0?"Sovtimer":("Timer · "+((left+59999)/60000)+" min"));}
        handler.postDelayed(this,1000);
    }};
    @Override public void onCreate(Bundle b){super.onCreate(b); prefs=getSharedPreferences("luma",0); selected=prefs.getInt("selected",0); audio=(AudioManager)getSystemService(AUDIO_SERVICE);
        focus=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setOnAudioFocusChangeListener(change->{if(change<0)pause();}).build();
        getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg); build(); handler.post(ticker);
    }
    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setFontFeatureSettings("kern");return t;}
    GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    Button button(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(white);b.setTextSize(14);b.setBackground(shape(panel,24));b.setMinHeight(dp(48));return b;}
    void gap(LinearLayout p,int h){Space s=new Space(this);p.addView(s,new LinearLayout.LayoutParams(1,dp(h)));}
    void build(){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(bg);
        root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(24),dp(26),dp(24),dp(32));scroll.addView(root);
        root.setOnApplyWindowInsetsListener((v,insets)->{root.setPadding(dp(24),dp(16)+insets.getSystemWindowInsetTop(),dp(24),dp(24)+insets.getSystemWindowInsetBottom());return insets;});setContentView(scroll);
        TextView logo=text("◒  L U M A",18,mint);root.addView(logo);gap(root,32);
        root.addView(text("Lite mer lugn.\nLite mer du.",36,white));gap(root,12);root.addView(text("Ljudlandskap för din egen stund.",15,muted));gap(root,26);
        LinearLayout tabs=new LinearLayout(this);Button all=button("Utforska"), fav=button("♡  Favoriter");tabs.addView(all,new LinearLayout.LayoutParams(0,dp(48),1));tabs.addView(fav,new LinearLayout.LayoutParams(0,dp(48),1));root.addView(tabs);
        all.setOnClickListener(v->{favoritesOnly=false;renderCards();});fav.setOnClickListener(v->{favoritesOnly=true;renderCards();});gap(root,24);
        cards=new LinearLayout(this);cards.setOrientation(1);root.addView(cards);renderCards();gap(root,12);
        LinearLayout player=new LinearLayout(this);player.setOrientation(1);player.setPadding(dp(20),dp(20),dp(20),dp(20));player.setBackground(shape(panel,24));root.addView(player);
        player.addView(text("D I N   S T U N D",11,mint));gap(player,14);title=text(names[selected],23,white);player.addView(title);subtitle=text(details[selected],13,muted);player.addView(subtitle);gap(player,16);
        LinearLayout controls=new LinearLayout(this);play=button("▶  Spela");play.setTextColor(bg);play.setBackground(shape(mint,28));heart=button(isFavorite(selected)?"♥":"♡");heart.setContentDescription("Lägg till eller ta bort favorit");controls.addView(play,new LinearLayout.LayoutParams(0,dp(56),3));controls.addView(heart,new LinearLayout.LayoutParams(0,dp(56),1));player.addView(controls);
        play.setOnClickListener(v->{if(playing)pause();else start();});heart.setOnClickListener(v->{prefs.edit().putBoolean("fav"+selected,!isFavorite(selected)).apply();heart.setText(isFavorite(selected)?"♥":"♡");renderCards();});gap(player,12);
        player.addView(text("Volym",12,muted));SeekBar volume=new SeekBar(this);volume.setMax(100);volume.setProgress(55);player.addView(volume);volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}public void onProgressChanged(SeekBar s,int p,boolean u){prefs.edit().putInt("volume",p).apply();if(track!=null)track.setVolume(p/100f);}});volume.setProgress(prefs.getInt("volume",55));
        timerLabel=button("Sovtimer");player.addView(timerLabel);timerLabel.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Avsluta din stund efter…").setItems(new String[]{"15 minuter","30 minuter","60 minuter","Stäng av timer"},(d,i)->{int[] mins={15,30,60,0};deadline=mins[i]==0?0:SystemClock.elapsedRealtime()+mins[i]*60000L;timerLabel.setText(mins[i]==0?"Sovtimer":"Timer · "+mins[i]+" min");}).show());
        gap(root,22);root.addView(text("OFFLINE. OSTÖRT. BARA DIN STUND.",10,muted));
    }
    boolean isFavorite(int i){return prefs.getBoolean("fav"+i,false);}
    void renderCards(){cards.removeAllViews();int count=0;for(int i=0;i<4;i++){if(favoritesOnly&&!isFavorite(i))continue;count++;final int index=i;
        LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setBackground(shape(panel,22));card.setClipToOutline(true);
        Scene art=new Scene(this,i);art.setContentDescription("Illustration: "+names[i]);card.addView(art,new LinearLayout.LayoutParams(-1,dp(i==0?200:140)));
        LinearLayout caption=new LinearLayout(this);caption.setPadding(dp(18),dp(16),dp(18),dp(16));caption.setOrientation(1);caption.addView(text(names[i]+"  ↗",21,white));caption.addView(text(details[i],13,muted));card.addView(caption);cards.addView(card);gap(cards,16);
        card.setOnClickListener(v->{pause();selected=index;prefs.edit().putInt("selected",selected).apply();title.setText(names[selected]);subtitle.setText(details[selected]);heart.setText(isFavorite(selected)?"♥":"♡");start();});}
        if(count==0){cards.addView(text("Dina lugna favoriter hamnar här.\nTryck på hjärtat i spelaren för att spara.",16,muted));gap(cards,24);}
    }
    void start(){if(audio.requestAudioFocus(focus)!=AudioManager.AUDIOFOCUS_REQUEST_GRANTED)return;
        try{if(track==null){short[] pcm=synthesize(selected);track=new AudioTrack.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setAudioFormat(new AudioFormat.Builder().setSampleRate(22050).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()).setTransferMode(AudioTrack.MODE_STATIC).setBufferSizeInBytes(pcm.length*2).build();track.write(pcm,0,pcm.length);track.setLoopPoints(0,pcm.length,-1);track.setVolume(prefs.getInt("volume",55)/100f);}track.play();playing=true;play.setText("Ⅱ  Pausa");}
        catch(RuntimeException e){pause();Toast.makeText(this,"Ljudet kunde inte startas. Försök igen.",Toast.LENGTH_LONG).show();}}
    void pause(){if(track!=null){track.stop();track.release();track=null;}playing=false;if(play!=null)play.setText("▶  Spela");if(audio!=null&&focus!=null)audio.abandonAudioFocusRequest(focus);}
    static short[] synthesize(int type){int n=22050*12;short[] out=new short[n];Random r=new Random(88+type);double low=0;
        for(int i=0;i<n;i++){double t=i/22050.0,noise=r.nextDouble()*2-1;low=.985*low+.015*noise;double wave=.45+.35*Math.sin(2*Math.PI*t/6);double s;
            if(type==0)s=low*wave*3+noise*.025;
            else if(type==1)s=noise*.15+low*.9;
            else if(type==2)s=low*1.6+Math.sin(2*Math.PI*1400*t)*Math.pow(Math.max(0,Math.sin(2*Math.PI*t/3)),28)*.022;
            else s=Math.sin(2*Math.PI*110*t)*.08+Math.sin(2*Math.PI*165*t)*.045+low*.45;
            double edge=Math.min(1,Math.min(i/(22050.0*.12),(n-1-i)/(22050.0*.12)));out[i]=(short)(Math.max(-1,Math.min(1,s))*16000*edge);}
        return out;}
    @Override protected void onStop(){super.onStop();pause();}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);pause();super.onDestroy();}
    static class Scene extends View{
        final int kind;Paint p=new Paint(3);Scene(Context c,int k){super(c);kind=k;}
        void color(int c){p.setColor(c);p.setShader(null);}
        @Override protected void onDraw(Canvas c){float w=getWidth(),h=getHeight();int[][] palettes={{0xff365864,0xffb9c9b1},{0xff303956,0xff9caca5},{0xff264d49,0xffa2c2a4},{0xff5d526f,0xffe3b18c}};int[] a=palettes[kind];p.setShader(new LinearGradient(0,0,0,h,a[0],a[1],Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);color(0xffe5dfb1);c.drawCircle(w*.74f,h*.29f,h*.13f,p);
            for(int layer=0;layer<4;layer++){color(new int[]{0xff668b86,0xff416c6a,0xff2b5056,0xff193b45}[layer]);Path path=new Path();path.moveTo(0,h);for(int x=0;x<=100;x++){float xx=w*x/100f;float yy=h*(.50f+layer*.13f)+(float)Math.sin(x*.06+layer*1.8+kind)*h*.11f;path.lineTo(xx,yy);}path.lineTo(w,h);path.close();c.drawPath(path,p);}
            if(kind==1){color(0x667dd0dc);p.setStrokeWidth(2);for(int x=0;x<40;x++){float xx=(x*41)%Math.max(1,w),yy=(x*67)%Math.max(1,h);c.drawLine(xx,yy,xx-8,yy+24,p);}}
            if(kind==2){color(0xff163b36);for(int x=0;x<12;x++){float xx=x*w/11;Path tree=new Path();tree.moveTo(xx,h*.35f+(x%3)*h*.09f);tree.lineTo(xx-w*.08f,h);tree.lineTo(xx+w*.08f,h);tree.close();c.drawPath(tree,p);}}
            color(0x99ffffff);p.setTextSize(h*.055f);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));c.drawText(new String[]{"O C E A N   /   0 1","R A I N   /   0 2","F O R E S T   /   0 3","D U S K   /   0 4"}[kind],w*.06f,h*.15f,p);
        }
    }
}
