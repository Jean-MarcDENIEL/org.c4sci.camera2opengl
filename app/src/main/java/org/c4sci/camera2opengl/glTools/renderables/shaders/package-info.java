/**
 * OpenGL Shaders can be automatically created by these helper classes:
 * <ul>
 *     <li>{@link org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderAttributes} represent input/output/uniform... variable declarations, and form a convention useful to mix stock and non stock shaders.</li>
 *     <li>{@link org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderVariable} represent shader variables, with their name, binding, attributes.</li>
 *     <li>{@link org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderCode} groups a part of the main function, useful other function and variable used by this code</li>
 *     <li>{@link org.c4sci.camera2opengl.glTools.renderables.shaders.AssembledShader} are made of several shader codes, assembled in a coherent form with some verifications.</li>
 *     <li>{@link org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderUtility} is a utility class capable of creating a complete shader program from AssembledShaders</li>
 * </ul>
 * Some stock shader codes are given in {@link org.c4sci.camera2opengl.glTools.renderables.shaders.stock.StockFragmentShaders} and {@link org.c4sci.camera2opengl.glTools.renderables.shaders.stock.StockFragmentShaders}.
 * They can be assembled to make basic but useful shader programs.
 */
