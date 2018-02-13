package com.factorsofx.stattrack.persist.mongo;

import com.factorsofx.stattrack.persist.UserProfileStore;
import com.factorsofx.stattrack.security.Permission;
import com.factorsofx.stattrack.security.UserProfile;
import com.mongodb.client.MongoDatabase;

import java.util.Arrays;
import java.util.Optional;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class MongoUserProfileStore extends AbstractMongoDataStore<UserProfile> implements UserProfileStore
{
    public MongoUserProfileStore(MongoDatabase db, String collectionName)
    {
        super(db, collectionName, UserProfile.class);
    }

    @Override
    public UserProfile getGuildProfileForUser(long guildId, long userId)
    {
        UserProfile profile = collection.find(and(eq("guildId", guildId), eq("userId", userId))).first();
        if(profile == null)
        {
            profile = new UserProfile(userId, guildId, Arrays.asList(Permission.DEFAULT_PERMS));
            collection.insertOne(profile);
        }
        return profile;
    }

    @Override
    public void updateGuildProfileForUser(UserProfile profile)
    {
        collection.replaceOne(and(eq("guildId", profile.getGuildId()), eq("userId", profile.getUserId())), profile);
    }
}
