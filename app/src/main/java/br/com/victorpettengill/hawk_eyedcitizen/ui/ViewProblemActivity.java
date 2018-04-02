package br.com.victorpettengill.hawk_eyedcitizen.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import br.com.victorpettengill.hawk_eyedcitizen.R;
import br.com.victorpettengill.hawk_eyedcitizen.beans.Problem;
import br.com.victorpettengill.hawk_eyedcitizen.beans.User;
import br.com.victorpettengill.hawk_eyedcitizen.dao.ProblemDao;
import br.com.victorpettengill.hawk_eyedcitizen.dao.UserDao;
import br.com.victorpettengill.hawk_eyedcitizen.listeners.DaoListener;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ViewProblemActivity extends AppCompatActivity {

    @BindView(R.id.image) ImageView image;
    @BindView(R.id.user) TextView user;
    @BindView(R.id.category) TextView category;
    @BindView(R.id.description) TextView description;
    @BindView(R.id.clap) Button clap;
    @BindView(R.id.share) Button share;

    private Problem problem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_problem);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        problem = getIntent().getParcelableExtra("problem");

        if(problem.getImage() != null) {
            image.setVisibility(View.VISIBLE);
            Picasso.with(ViewProblemActivity.this).load(problem.getImage()).into(image);

        } else {
            image.setVisibility(View.GONE);
        }

        description.setText(problem.getDescription());
        category.setText(problem.getCategory());

        if(problem.getUser().getName() == null) {

            UserDao.getInstance().getUserData(problem.getUser(), new DaoListener() {
                @Override
                public void onSuccess(Object object) {
                    super.onSuccess(object);

                    userInfo((User) object);

                }

                @Override
                public void onError(String message) {
                    super.onError(message);
                }
            });

        } else {
            userInfo(problem.getUser());
        }

        if(User.getInstance() != null && problem.getUser().getUid().equals(User.getInstance().getUid())) {

//            clap.setVisibility(View.GONE);

        }

    }

    @OnClick(R.id.clap) void clapProblem() {

        ProblemDao.getInstance().clapProblem(problem, User.getInstance(), new DaoListener() {
            @Override
            public void onSuccess(Object object) {
                super.onSuccess(object);

            }

            @Override
            public void onError(String message) {
                super.onError(message);
            }
        });

    }

    @OnClick(R.id.problem_solved) void problemSolved() {

        ProblemDao.getInstance().solveProblem(problem, User.getInstance(), new DaoListener() {

            @Override
            public void onSuccess(Object object) {
                super.onSuccess(object);
            }

            @Override
            public void onError(String message) {
                super.onError(message);
            }

        });

    }

    @OnClick(R.id.share) void shareProblem() {



    }

    private void userInfo(User userObject){

        user.setText(userObject.getName());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();

        return super.onOptionsItemSelected(item);
    }
}
