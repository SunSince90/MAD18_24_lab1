package mad24.polito.it.fragments.viewbook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.security.acl.Owner;
import java.util.Locale;

import mad24.polito.it.BooksActivity;
import mad24.polito.it.R;
import mad24.polito.it.fragments.FragmentLoadingListener;
import mad24.polito.it.fragments.FragmentWithLoadingListener;
import mad24.polito.it.chats.ChatActivity;
import mad24.polito.it.models.Book;
import mad24.polito.it.models.Chat;
import mad24.polito.it.models.UserMail;
import mad24.polito.it.registrationmail.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BookOwnerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BookOwnerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookOwnerFragment extends Fragment
{
    private static final String FIREBASE_DATABASE_LOCATION_USERS = BooksActivity.FIREBASE_DATABASE_LOCATION_USERS;

    private String UID = null;
    private DatabaseReference DB = null;
    private View RootView = null;
    private UserMail User = null;
    private StorageReference Storage = null;
    private Book TheBook = null;
    private String OwnerImage = null;

    private TextView stars = null;
    private me.zhanghai.android.materialratingbar.MaterialRatingBar ratingBar;
    private TextView ratingNumber = null;
    private Button commentsButton = null;

    //private OnFragmentInteractionListener mListener;

    public BookOwnerFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BookOwnerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BookOwnerFragment newInstance() {
        BookOwnerFragment fragment = new BookOwnerFragment();
        //Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            UID = getArguments().getString("owner");
            DB = FirebaseDatabase.getInstance().getReference()
                    .child(FIREBASE_DATABASE_LOCATION_USERS +"/" + UID);
            TheBook = new Gson().fromJson(getArguments().getString("book"), Book.class);
            Storage = FirebaseStorage.getInstance().getReference("profile_pictures").child(UID + ".jpg");
        }
    }

    private void loadAndInjectUser()
    {
        DB.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                User = dataSnapshot.getValue(UserMail.class);
                injectUser();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d("VIEWBOOK", "can't load user");
            }
        });
    }

    private void injectUser()
    {
        if(User == null) return;

        //  Put the owner's pic
        if(OwnerImage == null)
        {
            Storage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
            {
                @Override
                public void onSuccess(Uri uri)
                {
                    //  TODO: check for caching options.
                    Glide.with(getContext()).load(uri).into((ImageView) RootView.findViewById(R.id.ownerPic));
                    OwnerImage = uri.toString();
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    //  Failed...
                }
            });
        }
        else Glide.with(getContext()).load(OwnerImage).into((ImageView) RootView.findViewById(R.id.ownerPic));

        //  The name
        ((TextView) RootView.findViewById(R.id.ownerName)).setText(User.getName());

        //  The Conditions
        ((TextView) RootView.findViewById(R.id.bookConditions)).setText(String.format(getResources().getString(R.string.book_conditions_user), TheBook.getCondition()));

        //User rating
        String ratingStarsString = "0.0";
        float ratingStars = 0;

        //to avoid division by 0
        if(User.getRaters() > 0) {
            ratingStars = (float) User.getRating() / (float) User.getRaters();
            ratingStarsString = String.format("%.1f", ratingStars);
        }

        stars.setText(ratingStarsString);
        ratingBar.setRating(ratingStars);

        ratingNumber.setText(String.format(getString(R.string.user_ratings_count), User.getRaters()));

        commentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(UID == null)
                    return;

                //create dialog to rate the other user
                CommentDialog commentDialog = new CommentDialog(getActivity(), UID, User.getName());

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(commentDialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                commentDialog.show();
                commentDialog.getWindow().setAttributes(lp);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        RootView = inflater.inflate(R.layout.fragment_book_owner, container, false);
        stars = (TextView) RootView.findViewById(R.id.bookOwner_stars);
        ratingBar = (me.zhanghai.android.materialratingbar.MaterialRatingBar) RootView.findViewById(R.id.bookOwner_ratingBar);
        ratingNumber = (TextView) RootView.findViewById(R.id.bookOwner_ratingNumber);
        commentsButton = (Button) RootView.findViewById(R.id.bookOwner_comments);

        if(User == null) loadAndInjectUser();
        else injectUser();
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(TheBook.getUser_id())) return RootView;

        Button b = RootView.findViewById(R.id.startChat);
        b.setVisibility(View.VISIBLE);
        b.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                /*DatabaseReference chats =*/ FirebaseDatabase.getInstance()
                        .getReference()
                        .child("chats")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(TheBook.getUser_id())
                        .addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                //  Init the intent
                                Intent intent = new Intent(getActivity(), ChatActivity.class);
                                intent.putExtra("partner_id", TheBook.getUser_id());
                                intent.putExtra("book_id", TheBook.getBook_id());

                                //  Start the activity
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {
                                //  TODO: display an error message?
                                Log.d("CHAT", "on error");
                            }
                        });
            }
        });

        return RootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        //mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
