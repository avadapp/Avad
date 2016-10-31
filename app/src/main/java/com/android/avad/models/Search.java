package com.android.avad.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sagar_000 on 8/11/2016.
 */
@IgnoreExtraProperties
public class Search {

    public String fbUserId;
    public String title;
    public String likeToDo;
    public int starCount = 0;
    public Object timeStamp;

    public Map<String, Boolean> stars = new HashMap<>();

    public Search() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Search (String fbUserId, String likeToDo, Object timeStamp) {
        this.fbUserId = fbUserId;
        this.likeToDo = likeToDo;
        this.timeStamp = timeStamp;
    }

    public Long getCreatedTimestamp() {
        if (timeStamp instanceof Long) {
            return (Long) timeStamp;
        }
        else {
            return null;
        }
    }


    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("fbUserId", fbUserId);
        result.put("likeToDo", likeToDo);
        result.put("timeStamp",timeStamp);

        return result;
    }
    // [END post_to_map]

}
// [END post_class]
