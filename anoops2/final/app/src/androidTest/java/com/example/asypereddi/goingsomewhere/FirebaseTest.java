package com.example.asypereddi.goingsomewhere;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Tests the database and if the adding, reading, and modifying items is working by reading, adding,
 * and modifying a testItem item. And then deletes the item to check if that feature works as well.
 */

public class FirebaseTest {
    DatabaseReference mTestRef = FirebaseDatabase.getInstance().getReference().child("test");

    @Before
    @Test
    public void addItemTest() throws InterruptedException {
        final CountDownLatch writeSignal = new CountDownLatch(1);
        Item testItem = new Item("test", 2);
        mTestRef.setValue(testItem).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        writeSignal.countDown();
                    }
                });
        writeSignal.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void readItemTest() throws Exception {
        final CountDownLatch writeSignal = new CountDownLatch(1);
        mTestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final CountDownLatch writeSignal = new CountDownLatch(1);
                Item testItem = dataSnapshot.getValue(Item.class);
                assertEquals(2, testItem.getImportance());
                assertEquals("test", testItem.getName());
                writeSignal.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        writeSignal.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void modifyItemTest() throws Exception {
        final CountDownLatch writeSignal = new CountDownLatch(1);
        mTestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Item testItem = dataSnapshot.getValue(Item.class);
                testItem.setPacked(true);
                mTestRef.setValue(testItem);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mTestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final CountDownLatch writeSignal = new CountDownLatch(1);
                Item testItem = dataSnapshot.getValue(Item.class);
                assertTrue(!testItem.isPacked());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        writeSignal.await(10, TimeUnit.SECONDS);
    }

    @After
    @Test
    public void deleteItemTest() throws InterruptedException {
        final CountDownLatch writeSignal = new CountDownLatch(1);
        mTestRef.removeValue().
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        writeSignal.countDown();
                    }
                });
        writeSignal.await(10, TimeUnit.SECONDS);
    }
}
