package mad24.polito.it;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import mad24.polito.it.fragments.viewbook.ViewBookFragment;
import mad24.polito.it.models.Book;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private static final String FIREBASE_DATABASE_LOCATION_BOOKS = BooksActivity.FIREBASE_DATABASE_LOCATION_BOOKS;
    private StorageReference mStorageRef;

    Context mContext;
    List<Book> mData;
    HashSet<String> mDataId;

    View v;

    public RecyclerViewAdapter(Context mContext, List<Book> mData) {
        this.mContext = mContext;
        this.mData = new LinkedList<>();
        this.mDataId = new HashSet<>();

        for(Book newBook : mData) {
            if (!mDataId.contains(newBook.getBook_id())) {
                this.mDataId.add(newBook.getBook_id());
                this.mData.add(newBook);
            }
        }

        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        v = LayoutInflater.from(mContext).inflate(R.layout.adapter_books_layout, parent, false);

        MyViewHolder vHolder = new MyViewHolder(v);

        return vHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        holder.tv_title.setText(mData.get(position).getTitle());
        holder.tv_author.setText(mData.get(position).getAuthor());
        holder.tv_location.setText(mData.get(position).getLocation());

        //check if the book is on loan
        if(mData.get(position).getBorrowing_id() != null && !mData.get(position).getBorrowing_id().isEmpty())
            holder.tv_onloan.setText(R.string.bookDetail_bookOnLoan);
        else
            holder.tv_onloan.setText(R.string.bookDetail_bookAvailable);

        if(mData.get(position).isToRate()) {
            holder.tv_onloan.setText(R.string.bookDetail_bookReturnedRate);
        }

        String bookID = mData.get(position).getBook_id();
        Log.d("bookid", "I'm trying to get this book img: "+bookID);

        if(bookID != null) {

            StorageReference storageReference = mStorageRef.child("bookCovers").child(bookID+".jpg");

            /*Glide.with(mContext)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .into(holder.book_img);*/

            storageReference.getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageURL = uri.toString();

                            //  Set the url of cover *INSIDE* the object (so we won't have to query it again later).
                            //mData.get(holder.getAdapterPosition()).setBookImageLink(uri.toString());
                            //  IMPORTANT UPDATE: without this condition, an error is raised when clicking a notification.
                            if(!((BooksActivity) mContext).isFinishing())
                            Glide.with(holder.itemView.getContext()).load(imageURL).into(holder.book_img);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            holder.book_img.setImageDrawable(mContext.getResources().getDrawable(R.drawable.default_book_cover));
                        }
                    });
        }else{
            holder.book_img.setImageDrawable(mContext.getResources().getDrawable(R.drawable.default_book_cover));
        }

        //--------------------------------------
        //  Set item touch listener
        //--------------------------------------

        BooksActivity myActivity = (BooksActivity) v.getContext();
        if(myActivity.getCurrentFragment().equals("ProfileFragment") && mData.get(position).getUser_id().equals(FirebaseAuth.getInstance().getUid())) {
            final int p = position;
            holder.dots_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenu(holder.dots_menu, p);
                }
            });
        } else {
            holder.dots_menu.setVisibility(View.GONE);
        }

        if(holder.itemView.hasOnClickListeners()) return;

        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //----------------------------------
                //  Init
                //----------------------------------

                BooksActivity myActivity =  (BooksActivity) v.getContext();
                //  TODO: check if passing object as JSON makes sense: what if the object is too large?
                //  Pass the book's data here, so we won't have to download everything again
                //  Doing as JSON, so I won't have to write too many .putString etc :D
                Bundle args = new Bundle();
                args.putString("book", new Gson().toJson(mData.get(holder.getAdapterPosition())));

                //----------------------------------
                //  Start the fragment
                //----------------------------------

                if(myActivity.getCurrentFragment().equals("BooksFragment")) {
                    Log.d("check_adapter", "BooksFragment");
                    args.putInt("fragment", 0);
                }
                else
                    if (myActivity.getCurrentFragment().equals("ProfileFragment")) {
                        Log.d("check_adapter", "ProfileFragment");
                        args.putInt("fragment", 1);
                    }else Log.d("check_adapter", "nothing: "+myActivity.getCurrentFragment());

                final ViewBookFragment b = new ViewBookFragment();
                b.setArguments(args);

                //----------------------------------
                //  Show me the book
                //----------------------------------

                myActivity.setViewBookFragment(b);
                myActivity.setFragment(b, "ViewBook");
                /*booksActivity.setViewBookFragment(b);
                booksActivity.setFragment(b, "ViewBook");*/
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void retrieveBooks(List<String> books_id) {

        Query query;

        for(String b : books_id){
            if(mDataId.contains(b))
                continue;
            else
                mDataId.add(b);

            query = FirebaseDatabase.getInstance().getReference()
                    .child(FIREBASE_DATABASE_LOCATION_BOOKS)
                    .child(b);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Book book = dataSnapshot.getValue(Book.class);
                    /*for (DataSnapshot bookSnapshot : dataSnapshot.getChildren()) {
                        books.add(bookSnapshot.getValue(Book.class));
                    }*/



                    //Log.d("booksfragment", "adding "+books.size()+" books");
                    mData.add(book);
                    notifyItemInserted(mData.size());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    public void retrieveBooksAndToRate(List<String> books_id, final HashMap<String, Boolean> isToRate, final HashMap<String, String> borrowingIds) {
        //this.isToRate = isToRate;
        //this.borrowingIds = borrowingIds;

        Query query;

        final int position = 0;
        for(String b : books_id){
            if(mDataId.contains(b))
                continue;
            else
                mDataId.add(b);

            query = FirebaseDatabase.getInstance().getReference()
                    .child(FIREBASE_DATABASE_LOCATION_BOOKS)
                    .child(b);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    Book book = dataSnapshot.getValue(Book.class);

                    if(book == null)
                        return;

                    //Log.d("booksfragment", "adding "+books.size()+" books");
                    if(isToRate.get(book.getBook_id()) != null && isToRate.get(book.getBook_id()).booleanValue() == true) {
                        book.setToRate(true);
                        book.setBorrowing_id(borrowingIds.get(book.getBook_id()));
                    }

                    mData.add(book);
                    notifyItemInserted(mData.size());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    public String getLastItemId() {
        Log.d("booksfragment", "I've found "+mData.size()+" items");

        return mData.get(mData.size() - 1).getBook_id();
    }

    public void addAll(List<Book> newBooks) {
        int initialSize = mData.size();

        int count = 0;
        for(Book newBook : newBooks) {
            if(!mDataId.contains(newBook.getBook_id()) ) {
                mDataId.add(newBook.getBook_id());
                mData.add(newBook);
                count++;
            }
        }

        if(count == 0)
            return;

        notifyItemRangeInserted(initialSize, count);
    }

    public void add(Book book) {
        int initialSize = mData.size();
        if(mDataId.contains(book.getBook_id()))
            return;

        mData.add(book);
        mDataId.add(book.getBook_id());
        notifyItemRangeInserted(initialSize, 1);
    }

    public boolean contains(Book book) {
       return mDataId.contains(book.getBook_id());
    }

    public void clearAll() {
        int initialSize = mData.size();

        if(initialSize == 0)
            return;

        mDataId.clear();
        mData.clear();

        notifyItemRangeRemoved(0, initialSize);
    }

    private void showPopupMenu(View view, int position){
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.dots_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(position));
        popup.show();
    }

    public class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener{
        private int position;

        public MyMenuItemClickListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch(item.getItemId()){
                case R.id.remove_book:
                    removeBook(position);
                    return true;
            }
            return false;
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        private BooksActivity myActivity;
        private int fragment;

        private TextView tv_title;
        private TextView tv_author;
        private TextView tv_location;
        private TextView tv_onloan;
        private ImageView book_img;
        private ImageButton dots_menu;

        public MyViewHolder(View itemView){
            super(itemView);

            myActivity = (BooksActivity) itemView.getContext();
            if(myActivity.getCurrentFragment().equals("ProfileFragment"))
                fragment = 0;
            else
            if(myActivity.getCurrentFragment().equals("BooksFragment"))
                fragment = 1;

            tv_title = (TextView) itemView.findViewById(R.id.book_title);
            tv_author = (TextView) itemView.findViewById(R.id.book_author);
            tv_location = (TextView) itemView.findViewById(R.id.book_location);
            tv_onloan = (TextView) itemView.findViewById(R.id.book_onloan);
            book_img = (ImageView) itemView.findViewById(R.id.book_img);
            dots_menu = (ImageButton) itemView.findViewById(R.id.dots_menu);

            if(fragment == 0){
                dots_menu.setVisibility(View.VISIBLE);
            }
            else dots_menu.setVisibility(View.GONE);
        }
    }

    private void removeBook(final int position) {
        if(mData.get(position).getBorrowing_id() != null && !mData.get(position).getBorrowing_id().isEmpty()) {
            showDialog(v.getResources().getString(R.string.myBooks_cantRemoveBookOnLoan));
            return;
        }

        //create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

        //  Set title and message
        builder.setTitle(R.string.myBooks_sureRemoveBook);

        //  Set negative button
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        //  Set positive button
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                final Book b = mData.get(position);

                Task task = FirebaseDatabase.getInstance().getReference()
                        .child(FIREBASE_DATABASE_LOCATION_BOOKS)
                        .child(b.getBook_id())
                        .removeValue();

                task.addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        showDialog(v.getResources().getString(R.string.myBooks_bookRemoved));

                        mData.remove(position);
                        mDataId.remove(b.getBook_id());
                        notifyItemRemoved(position);
                    }
                });

                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showDialog(v.getResources().getString(R.string.myBooks_errorRemovingBook));
                    }
                });
            }
        });

        //show the AlertDialog on the screen
        builder.show();
    }

    private void showDialog(String title) {
        new AlertDialog.Builder(v.getContext())
                .setTitle(title)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

}






