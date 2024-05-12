package net.fxnt.fxntstorage.util;

import net.fxnt.fxntstorage.backpacks.main.BackPackBlock;

import java.util.ArrayList;
import java.util.List;

public class Util {

    // Storage Box Size
    public static final int IRON_STORAGE_BOX_SIZE = 60;
    public static final int ANDESITE_STORAGE_BOX_SIZE = 84;
    public static final int COPPER_STORAGE_BOX_SIZE = 108;
    public static final int BRASS_STORAGE_BOX_SIZE = 132;
    public static final int HARDENED_STORAGE_BOX_SIZE = 156;

    // Back Pack Size
    public static final int IRON_BACKPACK_STACK_SIZE = 128;
    public static final int ANDESITE_BACKPACK_STACK_SIZE = 256;
    public static final int COPPER_BACKPACK_STACK_SIZE = 512;
    public static final int BRASS_BACKPACK_STACK_SIZE = 1024;
    public static final int HARDENED_BACKPACK_STACK_SIZE = 2048;

    //public static final int MAX_STACK_SIZE = 1024;

    public static final byte BACKPACK_ON_BACK = 1;
    public static final byte BACKPACK_IN_HAND = 2;
    public static final byte BACKPACK_AS_BLOCK = 3;

    // BackPack Upgrades
    public static final String BLANK_UPGRADE = "back_pack_blank_upgrade";
    public static final String MAGNET_UPGRADE = "back_pack_magnet_upgrade";
    public static final String MAGNET_UPGRADE_DEACTIVATED = "back_pack_magnet_upgrade_deactivated";
    public static final String PICKBLOCK_UPGRADE = "back_pack_pickblock_upgrade";
    public static final String PICKBLOCK_UPGRADE_DEACTIVATED = "back_pack_pickblock_upgrade_deactivated";
    public static final String ITEMPICKUP_UPGRADE = "back_pack_itempickup_upgrade";
    public static final String ITEMPICKUP_UPGRADE_DEACTIVATED = "back_pack_itempickup_upgrade_deactivated";
    public static final String FLIGHT_UPGRADE = "back_pack_flight_upgrade";
    public static final String FLIGHT_UPGRADE_DEACTIVATED = "back_pack_flight_upgrade_deactivated";
    public static final String REFILL_UPGRADE = "back_pack_refill_upgrade";
    public static final String REFILL_UPGRADE_DEACTIVATED = "back_pack_refill_upgrade_deactivated";
    public static final String FEEDER_UPGRADE = "back_pack_feeder_upgrade";
    public static final String FEEDER_UPGRADE_DEACTIVATED = "back_pack_feeder_upgrade_deactivated";
    public static final String TOOLSWAP_UPGRADE = "back_pack_toolswap_upgrade";
    public static final String TOOLSWAP_UPGRADE_DEACTIVATED = "back_pack_toolswap_upgrade_deactivated";
    public static final String FALLDAMAGE_UPGRADE = "back_pack_falldamage_upgrade";
    public static final String FALLDAMAGE_UPGRADE_DEACTIVATED = "back_pack_falldamage_upgrade_deactivated";

    // BackPack Compartment Sizes
    public static final int ITEM_SLOT_START_RANGE = 0;
    public static final int ITEM_SLOT_END_RANGE = BackPackBlock.getContainerSlotCount();

    public static final int TOOL_SLOT_START_RANGE = ITEM_SLOT_END_RANGE;

    public static final int TOOL_SLOT_END_RANGE = TOOL_SLOT_START_RANGE + BackPackBlock.getToolSlotCount();

    public static final int UPGRADE_SLOT_START_RANGE = TOOL_SLOT_END_RANGE;

    public static final int UPGRADE_SLOT_END_RANGE = UPGRADE_SLOT_START_RANGE + BackPackBlock.getUpgradeSlotCount();


    // Menus
    public static final int SLOT_SIZE = 18;
    public static final int CONTAINER_HEADER_HEIGHT = 17;

    // Key Bind Bytes
    public static byte OPEN_BACKPACK = 0;
    public static byte TOGGLE_HOVER = 1;


    public static String formatNumber(int number) {
        if (number < 10_000) {
            return String.valueOf(number); // Numbers less than 10,000 are shown as the full integer
        } else if (number < 1_000_000) {
            // For numbers between 10,000 and 999,999, display as "X.XXk" or "XXXk"
            if (number % 1000 == 0) {
                return String.format("%dk", number / 1000); // Exact thousands
            } else {
                return String.format("%.2fk", number / 1000.0); // Otherwise, format to two decimal places
            }
        } else {
            // For numbers 1,000,000 or greater, display as "1.0M" or more
            if (number % 1_000_000 == 0) {
                return String.format("%dM", number / 1_000_000); // Exact millions
            } else {
                return String.format("%.2fM", number / 1_000_000.0); // Otherwise, format to two decimal places
            }
        }
    }

    public static List<String> wrapText(String text, int maxLineLength) {
        List<String> lines = new ArrayList<>();
        if (text == null) {
            return lines;
        }

        // Split the text into segments based on explicit line breaks.
        String[] segments = text.split("\n", -1); // Using limit -1 to preserve trailing empty strings

        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            if (segment.isEmpty()) {
                // Handle case where \n is at the end or there are consecutive \n
                lines.add("");
                continue;
            }

            // Split the segment into words based on spaces.
            String[] words = segment.split("\\s+");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                // Check if adding this word would exceed the line length.
                if (currentLine.length() + word.length() + 1 > maxLineLength) {
                    if (!currentLine.isEmpty()) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder();
                    }
                    // If a single word is longer than maxLineLength, split the word itself.
                    while (word.length() > maxLineLength) {
                        lines.add(word.substring(0, maxLineLength));
                        word = word.substring(maxLineLength);
                    }
                    currentLine.append(word);
                } else {
                    // Append a space before the word if it's not the first word on the line.
                    if (!currentLine.isEmpty()) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                }
            }

            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }
            // Add an explicit line break after processing each segment, only if it's not the last or empty segment
            if (i < segments.length - 1) {
                //lines.add("");
            }
        }

        return lines;
    }
}
