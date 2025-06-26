//package io.github.mwnlgo.pbo.components;
//
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.utils.TimeUtils;
//import io.github.mwnlgo.pbo.entities.Player;
//
//public class PlayerSpecialAttack extends PlayerAttackComponent {
//    private static final String TAG = "PlayerSpecialAttack";
//    public PlayerSpecialAttack(Player player) {
//        super(player);
//        setCooldown(5.0f);
//    }
//
//    @Override
//    public void attack() {
//        if (TimeUtils.millis() - getLastAttackTime() > getCooldown() * 1000) {
//            Gdx.app.log(TAG, "Special Attack success!");
//
//            // TODO: Logika serangan spesial
//
//            setLastAttackTime(TimeUtils.millis());
//        }
//    }
//}
