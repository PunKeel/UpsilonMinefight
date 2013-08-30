package fr.PunKeel.Upsilon;

import org.bukkit.Bukkit;

import java.util.ArrayList;

public class VoteKickHolder {
    int votes, last_vote;
    ArrayList<String> voters = new ArrayList<>();

    VoteKickHolder() {
        this.votes = 0;
        this.last_vote = 0;
    }

    public void reset() {
        this.votes = 0;
        this.last_vote = 0;
        this.voters.clear();
    }

    public boolean add(String p) {
        if (last_vote > Main.getTimestamp() + 70) reset();
        if (voters.contains(p)) return false;
        voters.add(p);
        votes++;
        last_vote = Main.getTimestamp();
        return true;
    }

    public boolean shouldKick() {
        int online = Bukkit.getOnlinePlayers().length;
        if (online < 10) {
            return votes > online / 2;
        } else {
            return votes >= online / 3;
        }
    }
}
