package me.josielcm.jcm.api.formats;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.Getter;
import me.josielcm.jcm.Base;

public class ParticleTextCreator {

    private static final Base plugin = Base.getInstance();
    private static final Map<Character, boolean[][]> characterMatrices = initializeCharacters();
    
    // Constants for character display
    private static final int CHAR_HEIGHT = 5;
    private static final int CHAR_WIDTH = 5;
    private static final double PARTICLE_SPACING = 0.2;

    @Getter
    private static final Map<Integer, BukkitRunnable> texts = new HashMap<>();
    
    /**
     * Creates a text display using particles at a specified location
     * @param location The base location for the text
     * @param text The text to display (will be converted to uppercase)
     * @param particle The particle type to use
     * @param duration Duration in seconds (use -1 for infinite)
     * @param interval Update interval in ticks
     */
    public static void createParticleText(int id, Location location, String text, Particle particle, int duration, int interval) {
        final String upperText = text.toUpperCase();
        
        BukkitRunnable particleTask = new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = duration == -1 ? -1 : duration * 20;
            
            @Override
            public void run() {
                // Stop if the duration has elapsed
                if (maxTicks != -1 && ticks >= maxTicks) {
                    this.cancel();
                    return;
                }
                
                // Display particles for each character
                double xOffset = 0;
                
                for (char c : upperText.toCharArray()) {
                    boolean[][] matrix = characterMatrices.getOrDefault(c, characterMatrices.get(' '));
                    
                    for (int y = 0; y < CHAR_HEIGHT; y++) {
                        for (int x = 0; x < CHAR_WIDTH; x++) {
                            if (matrix[y][x]) {
                                // Calculate position with offsets
                                double xPos = location.getX() + xOffset + (x * PARTICLE_SPACING);
                                double yPos = location.getY() + ((CHAR_HEIGHT - y) * PARTICLE_SPACING);
                                double zPos = location.getZ();
                                
                                // Spawn particle
                                location.getWorld().spawnParticle(
                                    particle,
                                    xPos, yPos, zPos,
                                    1, 0, 0, 0, 0
                                );
                            }
                        }
                    }
                    
                    // Move to next character position (add spacing between characters)
                    xOffset += (CHAR_WIDTH + 1) * PARTICLE_SPACING;
                }
                
                ticks += interval;
            }
        };
        
        // Start the runnable task
        particleTask.runTaskTimer(plugin, 0, interval);
        texts.put(id, particleTask);
    }

    public static void stop(int id) {
        if (texts.containsKey(id)) {
            texts.get(id).cancel();
            texts.remove(id);
        }
    }

    public static void stopAll() {
        for (Integer id : texts.keySet()) {
            texts.get(id).cancel();
        }

        texts.clear();
    }
    
    /**
     * Initialize character matrices for uppercase letters and common symbols
     */
    private static Map<Character, boolean[][]> initializeCharacters() {
        Map<Character, boolean[][]> chars = new HashMap<>();
        
        // Space
        chars.put(' ', new boolean[][]{
            {false, false, false, false, false},
            {false, false, false, false, false},
            {false, false, false, false, false},
            {false, false, false, false, false},
            {false, false, false, false, false}
        });
        
        // Letter A
        chars.put('A', new boolean[][]{
            {false, true, true, true, false},
            {true, false, false, false, true},
            {true, true, true, true, true},
            {true, false, false, false, true},
            {true, false, false, false, true}
        });
        
        // Letter B
        chars.put('B', new boolean[][]{
            {true, true, true, true, false},
            {true, false, false, false, true},
            {true, true, true, true, false},
            {true, false, false, false, true},
            {true, true, true, true, false}
        });
        
        // Letter C
        chars.put('C', new boolean[][]{
            {false, true, true, true, false},
            {true, false, false, false, true},
            {true, false, false, false, false},
            {true, false, false, false, true},
            {false, true, true, true, false}
        });
        
        // Letter D
        chars.put('D', new boolean[][]{
            {true, true, true, true, false},
            {true, false, false, false, true},
            {true, false, false, false, true},
            {true, false, false, false, true},
            {true, true, true, true, false}
        });
        
        // Letter E
        chars.put('E', new boolean[][]{
            {true, true, true, true, true},
            {true, false, false, false, false},
            {true, true, true, true, false},
            {true, false, false, false, false},
            {true, true, true, true, true}
        });
        
        // Letter F
        chars.put('F', new boolean[][]{
            {true, true, true, true, true},
            {true, false, false, false, false},
            {true, true, true, true, false},
            {true, false, false, false, false},
            {true, false, false, false, false}
        });
        
        // Letter G
        chars.put('G', new boolean[][]{
            {false, true, true, true, false},
            {true, false, false, false, false},
            {true, false, true, true, true},
            {true, false, false, false, true},
            {false, true, true, true, false}
        });
        
        // Letter H
        chars.put('H', new boolean[][]{
            {true, false, false, false, true},
            {true, false, false, false, true},
            {true, true, true, true, true},
            {true, false, false, false, true},
            {true, false, false, false, true}
        });
        
        // Letter I
        chars.put('I', new boolean[][]{
            {true, true, true, true, true},
            {false, false, true, false, false},
            {false, false, true, false, false},
            {false, false, true, false, false},
            {true, true, true, true, true}
        });
        
        // Letter J
        chars.put('J', new boolean[][]{
            {false, false, false, false, true},
            {false, false, false, false, true},
            {false, false, false, false, true},
            {true, false, false, false, true},
            {false, true, true, true, false}
        });
        
        // Letter K
        chars.put('K', new boolean[][]{
            {true, false, false, false, true},
            {true, false, false, true, false},
            {true, true, true, false, false},
            {true, false, false, true, false},
            {true, false, false, false, true}
        });
        
        // Letter L
        chars.put('L', new boolean[][]{
            {true, false, false, false, false},
            {true, false, false, false, false},
            {true, false, false, false, false},
            {true, false, false, false, false},
            {true, true, true, true, true}
        });
        
        // Letter M
        chars.put('M', new boolean[][]{
            {true, false, false, false, true},
            {true, true, false, true, true},
            {true, false, true, false, true},
            {true, false, false, false, true},
            {true, false, false, false, true}
        });
        
        // Letter N
        chars.put('N', new boolean[][]{
            {true, false, false, false, true},
            {true, true, false, false, true},
            {true, false, true, false, true},
            {true, false, false, true, true},
            {true, false, false, false, true}
        });
        
        // Letter O
        chars.put('O', new boolean[][]{
            {false, true, true, true, false},
            {true, false, false, false, true},
            {true, false, false, false, true},
            {true, false, false, false, true},
            {false, true, true, true, false}
        });
        
        // Letter P
        chars.put('P', new boolean[][]{
            {true, true, true, true, false},
            {true, false, false, false, true},
            {true, true, true, true, false},
            {true, false, false, false, false},
            {true, false, false, false, false}
        });
        
        // Letter Q
        chars.put('Q', new boolean[][]{
            {false, true, true, true, false},
            {true, false, false, false, true},
            {true, false, false, false, true},
            {true, false, false, true, false},
            {false, true, true, false, true}
        });
        
        // Letter R
        chars.put('R', new boolean[][]{
            {true, true, true, true, false},
            {true, false, false, false, true},
            {true, true, true, true, false},
            {true, false, false, true, false},
            {true, false, false, false, true}
        });
        
        // Letter S
        chars.put('S', new boolean[][]{
            {false, true, true, true, true},
            {true, false, false, false, false},
            {false, true, true, true, false},
            {false, false, false, false, true},
            {true, true, true, true, false}
        });
        
        // Letter T
        chars.put('T', new boolean[][]{
            {true, true, true, true, true},
            {false, false, true, false, false},
            {false, false, true, false, false},
            {false, false, true, false, false},
            {false, false, true, false, false}
        });
        
        // Letter U
        chars.put('U', new boolean[][]{
            {true, false, false, false, true},
            {true, false, false, false, true},
            {true, false, false, false, true},
            {true, false, false, false, true},
            {false, true, true, true, false}
        });
        
        // Letter V
        chars.put('V', new boolean[][]{
            {true, false, false, false, true},
            {true, false, false, false, true},
            {true, false, false, false, true},
            {false, true, false, true, false},
            {false, false, true, false, false}
        });
        
        // Letter W
        chars.put('W', new boolean[][]{
            {true, false, false, false, true},
            {true, false, false, false, true},
            {true, false, true, false, true},
            {true, true, false, true, true},
            {true, false, false, false, true}
        });
        
        // Letter X
        chars.put('X', new boolean[][]{
            {true, false, false, false, true},
            {false, true, false, true, false},
            {false, false, true, false, false},
            {false, true, false, true, false},
            {true, false, false, false, true}
        });
        
        // Letter Y
        chars.put('Y', new boolean[][]{
            {true, false, false, false, true},
            {false, true, false, true, false},
            {false, false, true, false, false},
            {false, false, true, false, false},
            {false, false, true, false, false}
        });
        
        // Letter Z
        chars.put('Z', new boolean[][]{
            {true, true, true, true, true},
            {false, false, false, true, false},
            {false, false, true, false, false},
            {false, true, false, false, false},
            {true, true, true, true, true}
        });
        
        // Numbers
        // Number 0
        chars.put('0', new boolean[][]{
            {false, true, true, true, false},
            {true, false, false, true, true},
            {true, false, true, false, true},
            {true, true, false, false, true},
            {false, true, true, true, false}
        });
        
        // Number 1
        chars.put('1', new boolean[][]{
            {false, false, true, false, false},
            {false, true, true, false, false},
            {false, false, true, false, false},
            {false, false, true, false, false},
            {false, true, true, true, false}
        });
        
        // Number 2
        chars.put('2', new boolean[][]{
            {false, true, true, true, false},
            {true, false, false, false, true},
            {false, false, true, true, false},
            {false, true, false, false, false},
            {true, true, true, true, true}
        });
        
        // Number 3
        chars.put('3', new boolean[][]{
            {false, true, true, true, false},
            {true, false, false, false, true},
            {false, false, true, true, false},
            {true, false, false, false, true},
            {false, true, true, true, false}
        });
        
        // Number 4
        chars.put('4', new boolean[][]{
            {false, false, true, true, false},
            {false, true, false, true, false},
            {true, false, false, true, false},
            {true, true, true, true, true},
            {false, false, false, true, false}
        });
        
        // Number 5
        chars.put('5', new boolean[][]{
            {true, true, true, true, true},
            {true, false, false, false, false},
            {true, true, true, true, false},
            {false, false, false, false, true},
            {true, true, true, true, false}
        });
        
        // Number 6
        chars.put('6', new boolean[][]{
            {false, true, true, true, false},
            {true, false, false, false, false},
            {true, true, true, true, false},
            {true, false, false, false, true},
            {false, true, true, true, false}
        });
        
        // Number 7
        chars.put('7', new boolean[][]{
            {true, true, true, true, true},
            {false, false, false, true, false},
            {false, false, true, false, false},
            {false, true, false, false, false},
            {true, false, false, false, false}
        });
        
        // Number 8
        chars.put('8', new boolean[][]{
            {false, true, true, true, false},
            {true, false, false, false, true},
            {false, true, true, true, false},
            {true, false, false, false, true},
            {false, true, true, true, false}
        });
        
        // Number 9
        chars.put('9', new boolean[][]{
            {false, true, true, true, false},
            {true, false, false, false, true},
            {false, true, true, true, true},
            {false, false, false, false, true},
            {false, true, true, true, false}
        });
        
        // Special characters
        // Period .
        chars.put('.', new boolean[][]{
            {false, false, false, false, false},
            {false, false, false, false, false},
            {false, false, false, false, false},
            {false, false, false, false, false},
            {false, false, true, false, false}
        });
        
        // Comma ,
        chars.put(',', new boolean[][]{
            {false, false, false, false, false},
            {false, false, false, false, false},
            {false, false, false, false, false},
            {false, false, true, false, false},
            {false, true, false, false, false}
        });
        
        // Exclamation !
        chars.put('!', new boolean[][]{
            {false, false, true, false, false},
            {false, false, true, false, false},
            {false, false, true, false, false},
            {false, false, false, false, false},
            {false, false, true, false, false}
        });
        
        // Question ?
        chars.put('?', new boolean[][]{
            {false, true, true, true, false},
            {true, false, false, false, true},
            {false, false, false, true, false},
            {false, false, true, false, false},
            {false, false, true, false, false}
        });
        
        return chars;
    }


}
