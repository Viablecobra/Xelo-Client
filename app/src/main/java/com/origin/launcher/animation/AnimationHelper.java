package com.origin.launcher.animation;

import android.view.View;

import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.origin.launcher.databinding.ActivityMainBinding;

public class AnimationHelper {
    public static void prepareInitialStates(ActivityMainBinding binding) {
        float density = binding.getRoot().getResources().getDisplayMetrics().density;
        float startOffsetX = -32f * density;

        binding.header.setVisibility(View.INVISIBLE);
        binding.header.setAlpha(0f);

        binding.mainCard.setVisibility(View.INVISIBLE);
        binding.mainCard.setAlpha(0f);
        binding.mainCard.setTranslationX(startOffsetX);

        binding.modCard.setVisibility(View.INVISIBLE);
        binding.modCard.setAlpha(0f);
        binding.modCard.setTranslationX(startOffsetX);

        binding.toolsCard.setVisibility(View.INVISIBLE);
        binding.toolsCard.setAlpha(0f);
        binding.toolsCard.setTranslationX(startOffsetX);
    }

    public static void runInitializationSequence(ActivityMainBinding binding) {
        binding.header.postDelayed(() -> {
            binding.header.setVisibility(View.VISIBLE);
            SpringAnimation alphaAnim = new SpringAnimation(binding.header, DynamicAnimation.ALPHA, 1f);
            alphaAnim.setSpring(new SpringForce(1f)
                    .setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY)
                    .setStiffness(SpringForce.STIFFNESS_LOW));
            alphaAnim.start();
        }, 200);

        startCardEnter(binding.mainCard, 350);
        startCardEnter(binding.modCard, 450);
        startCardEnter(binding.toolsCard, 550);
    }

    private static void startCardEnter(View card, long delayMs) {
        card.postDelayed(() -> {
            card.setVisibility(View.VISIBLE);

            SpringAnimation txAnim = new SpringAnimation(card, DynamicAnimation.TRANSLATION_X, 0f);
            txAnim.setSpring(new SpringForce(0f)
                    .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
                    .setStiffness(SpringForce.STIFFNESS_LOW));

            SpringAnimation alphaAnim = new SpringAnimation(card, DynamicAnimation.ALPHA, 1f);
            alphaAnim.setSpring(new SpringForce(1f)
                    .setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY)
                    .setStiffness(SpringForce.STIFFNESS_LOW));

            txAnim.start();
            alphaAnim.start();
        }, delayMs);
    }
}