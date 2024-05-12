package net.fxnt.fxntstorage.cache;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BackPackShapeCache {
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
        shapes.put(Direction.NORTH, createShapeForDirection(Direction.NORTH));
        shapes.put(Direction.SOUTH, createShapeForDirection(Direction.SOUTH));
        shapes.put(Direction.EAST, createShapeForDirection(Direction.EAST));
        shapes.put(Direction.WEST, createShapeForDirection(Direction.WEST));
    }

    private static VoxelShape createShapeForDirection(Direction direction) {

        VoxelShape finalShape = Shapes.empty();

        // Define each box part of your shape
        List<VoxelShape> parts = Arrays.asList(
                Block.box(3, 0, 5, 13, 11, 11),
                Block.box(3.5, 0.5, 11, 12.5, 10.5, 11.25),
                Block.box(13, 0.5, 5.5, 13.25, 10.5, 10.5),
                Block.box(2.75, 0.5, 5.5, 3, 10.5, 10.5),
                Block.box(3.5, -0.25, 5.5, 12.5, 0, 10.5),
                Block.box(4, 0.5, 3,12, 7.5, 5),
                Block.box(5, 1.5, 2.5,11, 6.5, 3.5),
                Block.box(6, 14, 8,10, 14.25, 9),
                Block.box(9, 13.25, 8,10, 14, 9),
                Block.box(6, 13.25, 8,7, 14, 9),
                Block.box(3.5, 11, 7,12.5, 13, 10),
                Block.box(12.5, 11, 7.25,12.75, 12.8, 10),
                Block.box(3.25, 11, 7.25,3.5, 12.8, 10),
                Block.box(3.5, 11, 6,12.5, 12.5, 7),
                Block.box(4, 11, 5.9,12, 11.1, 6),
                Block.box(6, 6, 2.4,10, 6.1, 2.5),
                Block.box(9.9, 5.8, 2.4,10, 6, 2.5),
                Block.box(11.9, 11, 5.7,12, 11.1, 5.9),
                Block.box(3.5, 11, 10,12.5, 12.8, 11),
                Block.box(4.5, 7, 4,11.5, 8, 5),
                Block.box(9.5, 1, 11,11.5, 12, 11.75),
                Block.box(4.5, 1, 11,6.5, 12, 11.75),
                Block.box(9.75, 12, 11,11.25, 12.75, 11.25),
                Block.box(4.75, 12, 11,6.25, 12.75, 11.25),
                Block.box(4.75, 0.25, 11,6.25, 1.05, 11.4),
                Block.box(9.75, 0.25, 11,11.25, 1.05, 11.4)
        );

        // Rotate and combine all parts for the specified direction
        for (VoxelShape part : parts) {
            VoxelShape rotatedPart = rotateHorizontal(part, direction);
            finalShape = Shapes.or(finalShape, rotatedPart);
        }

        return finalShape.optimize();
    }

    public static VoxelShape getShape(Direction direction) {
        return shapes.get(direction);
    }

    private static VoxelShape rotateHorizontal(VoxelShape shape, Direction dir) {
        switch (dir) {
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