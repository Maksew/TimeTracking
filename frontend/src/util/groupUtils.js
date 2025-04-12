// src/utils/groupUtils.js

// Palette de couleurs pour les groupes
const groupColors = [
  '#673ab7', '#3f51b5', '#2196f3', '#9c27b0',
  '#5e35b1', '#3949ab', '#1e88e5',
  '#8e24aa', '#5e35b1', '#3949ab'
];

/**
 * Attribue une couleur à un groupe en fonction de son ID
 * @param {number|string} groupId - L'ID du groupe
 * @returns {string} - Le code couleur hexadécimal
 */
export function getGroupColor(groupId) {
  if (!groupId) return '#9c27b0'; // Couleur par défaut

  // Convertir l'ID en nombre si ce n'est pas déjà le cas
  const numericId = typeof groupId === 'number' ? groupId : parseInt(groupId, 10);

  // Utiliser le modulo pour obtenir un index dans le tableau de couleurs
  const colorIndex = (numericId || 0) % groupColors.length;

  return groupColors[colorIndex];
}

/**
 * Obtient le nom d'un groupe à partir de son ID
 * @param {number|string} groupId - L'ID du groupe
 * @param {Array} groups - La liste des groupes disponibles
 * @returns {string} - Le nom du groupe
 */
export function getGroupName(groupId, groups) {
  if (groupId === 'personnel') return 'Personnel';

  const group = groups.find(g => g.id === groupId);
  return group ? group.name : `Groupe #${groupId}`;
}
