package com.rick.rtfccore.transformers;

import com.rick.rtfcadditions.debug.DebugUtils;
import com.rick.rtfcadditions.utils.PotionsTweaker;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class NEIPotionsClassTransformer implements IClassTransformer
{
    private static ClassNode byteArrayToClassNode(byte[] basicClass)
    {
        ClassNode cn = new ClassNode();
        ClassReader cr = new ClassReader(basicClass);
        cr.accept(cn, ClassReader.SKIP_FRAMES);
        return cn;
    }

    private static byte[] classNodeToByteArray(ClassNode cn)
    {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cn.accept(cw);
        return cw.toByteArray();
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if (transformedName.equals("codechicken.nei.recipe.BrewingRecipeHandler")) {
            ClassNode cn = byteArrayToClassNode(basicClass);
            for (MethodNode mn : cn.methods) {
                if (mn.name.equals("searchPotions") && mn.desc.equals("()V")) {
                    transformSearchPotions(mn);
                    return classNodeToByteArray(cn);
                }
            }
            DebugUtils.logWarn("Failed to find the BrewingRecipeHandler.searchPotions method!");
        }
        return basicClass;
    }

    private void transformSearchPotions(MethodNode mn)
    {
        InsnList payload = new InsnList();
        payload.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(PotionsTweaker.class), "searchPotions", "()V", false));
        payload.add(new InsnNode(Opcodes.RETURN));
        mn.instructions.insertBefore(mn.instructions.getFirst(), payload);
        DebugUtils.logInfo("Successful injection in BrewingRecipeHandler.searchPotions");
    }

}
