package com.intricatech.slingball;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class InstructionActivity extends AppCompatActivity {

    private boolean proceedToGame;
    private boolean resumeLastGame;

    private ViewPager viewPager;

    private String[] pageTitles = new String[]{"The Ball...", "The Wall....", "Bonuses..."};
    private int[] imageResIds = new int[]{
            R.drawable.ball_instruct,
            R.drawable.ball_instruct,
            R.drawable.ball_instruct
            /*R.drawable.wall_collision_instruct,
            R.drawable.bonus_instruct*/};
    private Bitmap[] imageBitmaps = new Bitmap[pageTitles.length];
    private String[] instructionTexts = new String[]{
            "Hold one of the corner buttons!\n\nRelease the button to let " +
                    "the ball fly!",
            "Don't let the ball hit the edge! You'll lose energy!",
            "Hit these targets to get different rewards.\n\n" +
                "Press the button at the bottom to activate the reward!"};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction_2);

        Intent intent = getIntent();
        proceedToGame = intent.getBooleanExtra("PROCEED_TO_GAME", false);
        resumeLastGame = intent.getBooleanExtra("RESUME_LAST_GAME", false);

       /* BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        for (int i = 0; i < imageBitmaps.length; i++) {
            imageBitmaps[i] = BitmapFactory.decodeResource(getResources(), imageResIds[i], options);
        }

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new PagesAdapter(this));*/
    }

    @Override
    public void onBackPressed() {
        if (proceedToGame) {
            startGameDirectFromThisActivity();
        } else {
            super.onBackPressed();
        }
    }

    private void startGameDirectFromThisActivity() {
        Intent intent = new Intent(this, GameActivity.class);

        intent.putExtra("RESUME_LAST_GAME", resumeLastGame);
        startActivity(intent);
    }

    class PagesAdapter extends PagerAdapter {

        private Context context;

        PagesAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return pageTitles.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (View) object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View page = inflater.inflate(R.layout.view_page_template, null);
            ((TextView)page.findViewById(R.id.instruct_page_title)).setText(pageTitles[position]);

            //((ImageView)page.findViewById(R.id.instruct_page_imageview)).setImageResource(imageResIds[position]);
            ((ImageView)page.findViewById(R.id.instruct_page_imageview)).setImageBitmap(imageBitmaps[position]);

            ((TextView) page.findViewById(R.id.instructions_detail)).setText(instructionTexts[position]);
            ((ViewPager) container).addView(page, 0);

            return page;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            /*ImageView imageView = (ImageView) view.findViewById(R.id.instruct_page_imageview);
            BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
            if (bitmapDrawable != null && bitmapDrawable.getBitmap() != null) {
                bitmapDrawable.getBitmap().recycle();
            }*/
            ((ViewPager) container).removeView(view);
            view = null;


            object = null;
        }
    }
}
