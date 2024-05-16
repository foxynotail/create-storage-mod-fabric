package net.fxnt.fxntstorage.cache;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PasserShapeCache {
    private static final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

    static {
        initializeShapes();
    }

    public static void clearCache() {
        shapes.clear();
        initializeShapes();
    }

    private static void initializeShapes() {
        // Initialize and store shapes based on direction
        shapes.put(Direction.UP, createShapeForDirection(Direction.UP));
        shapes.put(Direction.DOWN, createShapeForDirection(Direction.DOWN));
        shapes.put(Direction.NORTH, createShapeForDirection(Direction.NORTH));
        shapes.put(Direction.SOUTH, createShapeForDirection(Direction.SOUTH));
        shapes.put(Direction.EAST, createShapeForDirection(Direction.EAST));
        shapes.put(Direction.WEST, createShapeForDirection(Direction.WEST));
    }

    private static VoxelShape createShapeForDirection(Direction direction) {

        VoxelShape finalShape = Shapes.empty();

        // Define each box part of your shape
        List<VoxelShape> parts = Arrays.asList(
                Block.box(4, 4, 0, 12, 12, 1),
                Block.box(5, 5, 1, 11, 11, 5),
                Block.box(4, 4, 5, 12, 12, 9),
                Block.box(3, 3, 9, 13, 13, 15),
                Block.box(2, 2, 15, 14, 14, 16)
        );

        // Rotate and combine all parts for the specified direction
        for (VoxelShape part : parts) {
            VoxelShape rotatedPart = rotate(part, direction);
            finalShape = Shapes.or(finalShape, rotatedPart);
        }

        return finalShape.optimize();
    }

    public static VoxelShape getShape(Direction direction) {
        return shapes.get(direction);
    }

    private static VoxelShape rotate(VoxelShape shape, Direction dir) {
        switch (dir) {
            case UP:
                // Rotate 270 degrees around X axis
                return Shapes.create(
                        shape.min(Direction.Axis.X), 1 - shape.max(Direction.Axis.Z), shape.min(Direction.Axis.Y),
                        shape.max(Direction.Axis.X), 1 - shape.min(Direction.Axis.Z), shape.max(Direction.Axis.Y));
            case DOWN:
                // Rotate 90 degrees around X axis
                return Shapes.create(
                        shape.min(Direction.Axis.X), shape.min(Direction.Axis.Z), shape.min(Direction.Axis.Y),
                        shape.max(Direction.Axis.X), shape.max(Direction.Axis.Z), shape.max(Direction.Axis.Y));
            case SOUTH:
                // Rotate 180 degrees around Y axis
                return Shapes.create(
                        1 - shape.max(Direction.Axis.X), shape.min(Direction.Axis.Y), 1 - shape.max(Direction.Axis.Z),
                        1 - shape.min(Direction.Axis.X), shape.max(Direction.Axis.Y), 1 - shape.min(Direction.Axis.Z));
            case WEST:
                // Rotate 270 degrees around Y axis
                return Shapes.create(
                        shape.min(Direction.Axis.Z), shape.min(Direction.Axis.Y), 1 - shape.max(Direction.Axis.X),
                        shape.max(Direction.Axis.Z), shape.max(Direction.Axis.Y), 1 - shape.min(Direction.Axis.X));
            case EAST:
                // Rotate 90 degrees around Y axis
                return Shapes.create(
                        1 - shape.max(Direction.Axis.Z), shape.min(Direction.Axis.Y), shape.min(Direction.Axis.X),
                        1 - shape.min(Direction.Axis.Z), shape.max(Direction.Axis.Y), shape.max(Direction.Axis.X));
            case NORTH:
            default:
                // No rotation needed
                return shape;
        }
    }


}