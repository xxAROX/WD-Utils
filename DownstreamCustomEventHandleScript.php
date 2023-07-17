<?php
use pocketmine\event\Listener;
use pocketmine\event\server\DataPacketReceiveEvent;
use pocketmine\network\mcpe\protocol\ClientboundPacket;
use pocketmine\network\mcpe\protocol\ServerboundPacket;
use pocketmine\network\mcpe\protocol\ScriptMessagePacket;
use pocketmine\player\Player;
use pocketmine\plugin\PluginBase;


/**
 * @name DownstreamCustomEventHandleScript
 * @main \DownstreamCustomEventHandleScript
 * @version 1.0.5
 * @api 5.3.0
 */
class DownstreamCustomEventHandleScript extends PluginBase implements Listener{
	public const IDENTIFIER = "waterdogpe:";

	public function onEnable(): void{
		$this->getServer()->getPluginManager()->registerEvents($this, $this);
	}

	public function DataPacketReceiveEvent(DataPacketReceiveEvent $event): void{
		$packet = $event->getPacket();
		$player = $event->getOrigin()->getPlayer();
		if (!$packet instanceof ScriptMessagePacket) return;
		switch (str_replace(DownstreamCustomEventHandleScript::IDENTIFIER, "", mb_strtolower($packet->eventName))) {
			case "dispatch_command":{
				$this->getServer()->dispatchCommand($player, $packet->eventData, true);
				break;
			}
			case "sync_permissions":{
				$pk = ScriptMessagePacket::create(DownstreamCustomEventHandleScript::IDENTIFIER . "sync_permissions", json_encode(array_map(fn($_) => $_->getPermission(), $player->getEffectivePermissions())));
				$player->getNetworkSession()->sendDataPacket($pk);
				break;
			}
		}
	}


	public static function dispatch_command(Player $player, string $command_line): void{
		if (str_starts_with($command_line, "/")) $command_line = substr($command_line, 1); // bypass the '/' confusion convos lmao
		$player->getNetworkSession()->sendDataPacket(ScriptMessagePacket::create(DownstreamCustomEventHandleScript::IDENTIFIER . "dispatch_command", $command_line));
	}


	public static function add_server(Player $player, string $server_name, string $server_address, int $server_port = 19132, ?string $server_public_address = null, int $server_public_port = 19132): void{
		$player->getNetworkSession()->sendDataPacket(ScriptMessagePacket::create(DownstreamCustomEventHandleScript::IDENTIFIER . "add_server", json_encode([
		    "server_name" => $server_name,
		    "server_address" => $server_address,
		    "server_port" => $server_port,
		    "server_public_address" => $server_public_address,
		    "server_public_port" => $server_public_port,
		]));
	}


	public static function remove_server(Player $player, string $server_name): void{
		$player->getNetworkSession()->sendDataPacket(ScriptMessagePacket::create(DownstreamCustomEventHandleScript::IDENTIFIER . "remove_server", json_encode([
		    "server_name" => $server_name,
		]));
	}
}
