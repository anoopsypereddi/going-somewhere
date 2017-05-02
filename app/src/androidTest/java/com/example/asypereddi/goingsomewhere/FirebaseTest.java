package com.example.asypereddi.goingsomewhere;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;

/**
 * Tests the database and if the adding trip function is working properly by adding a new trip to a
 * test location in the database.
 */

public class FirebaseTest {
    DatabaseReference mTestRef = FirebaseDatabase.getInstance().getReference().child("test");

    @Before
    @Test
    public void addTripTest() throws InterruptedException {
        final CountDownLatch writeSignal = new CountDownLatch(1);
        mTestRef.setValue("trip").
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        writeSignal.countDown();
                    }
                });
        writeSignal.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void readTripTest() throws Exception {
        final CountDownLatch writeSignal = new CountDownLatch(1);
        mTestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final CountDownLatch writeSignal = new CountDownLatch(1);
                assertEquals("trip", dataSnapshot.getValue(String.class));
                writeSignal.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        writeSignal.await(10, TimeUnit.SECONDS);
    }
}
