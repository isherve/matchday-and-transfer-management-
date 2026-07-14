-- Prefer each fixture venue from the home team's stadium when blank or default seed value.
UPDATE fixture f
SET stadium = (
    SELECT t.stadium FROM team t WHERE t.team_id = f.home_team_id
)
WHERE (f.stadium IS NULL OR f.stadium = 'Kigali Stadium')
  AND EXISTS (SELECT 1 FROM team t WHERE t.team_id = f.home_team_id AND t.stadium IS NOT NULL);
