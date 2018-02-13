package com.factorsofx.stattrack.persist;

import com.factorsofx.stattrack.security.UserProfile;

public interface UserProfileStore extends DataStore<UserProfile>
{
    UserProfile getGuildProfileForUser(long guildId, long userId);

    void updateGuildProfileForUser(UserProfile profile);
}
