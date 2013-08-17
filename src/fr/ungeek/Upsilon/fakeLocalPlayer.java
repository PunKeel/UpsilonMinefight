package fr.ungeek.Upsilon;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bags.BlockBag;

/**
 * User: PunKeel
 * Date: 6/9/13
 * Time: 10:18 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class fakeLocalPlayer extends LocalPlayer {
    LocalWorld W;

    protected fakeLocalPlayer(ServerInterface server, LocalWorld world) {
        super(server);
        W = world;
    }

    @Override
    public int getItemInHand() {
        return 0;
    }

    @Override
    public String getName() {
        return "FakePlayer";
    }

    @Override
    public WorldVector getPosition() {
        return new WorldVector(getWorld(), 0, 0, 0);
    }

    @Override
    public LocalWorld getWorld() {
        return W;
    }

    @Override
    public double getPitch() {
        return 0;
    }

    @Override
    public double getYaw() {
        return 0;
    }

    @Override
    public void giveItem(int i, int i2) {
    }

    @Override
    public void printRaw(String s) {
    }

    @Override
    public void printDebug(String s) {

    }

    @Override
    public void print(String s) {

    }

    @Override
    public void printError(String s) {

    }

    @Override
    public void setPosition(Vector vector, float v, float v2) {

    }

    @Override
    public String[] getGroups() {
        return new String[0];
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return null;
    }

    @Override
    public boolean hasPermission(String s) {
        return true;
    }
}
