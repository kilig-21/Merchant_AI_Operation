UPDATE sys_user
SET password_hash = '$2a$10$YTWS92aSsBPtJludozWhaumOZOI/Oky6YkWNd2lytGe3SNJCEytAC'
WHERE username IN ('consumer_001', 'merchant_a_admin', 'merchant_b_admin');